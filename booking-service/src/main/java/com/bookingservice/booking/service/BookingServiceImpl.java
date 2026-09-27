package com.bookingservice.booking.service;

import com.bookingservice.booking.entity.Booking;
import com.bookingservice.booking.entity.BookingStatus;
import com.bookingservice.booking.exception.BookingNotFoundException;
import com.bookingservice.booking.exception.RoomUnavailableException;
import com.bookingservice.booking.grpc.RoomInventoryClient;
import com.bookingservice.booking.repository.BookingRepository;
import com.bookingservice.booking.web.dto.CreateBookingRequest;
import com.bookingservice.booking.web.dto.RoomDto;
import com.hotel.roominventory.grpc.RequestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final RoomInventoryClient roomInventoryClient;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomInventoryClient roomInventoryClient) {
        this.bookingRepository = bookingRepository;
        this.roomInventoryClient = roomInventoryClient;
    }

    @Override
    public List<RoomDto> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return roomInventoryClient.listAvailableRooms(checkIn, checkOut);
    }

    @Override
    public Booking createBooking(CreateBookingRequest request) {
        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new IllegalArgumentException("guestName is required");
        }
        if (request.roomId() == null) {
            throw new IllegalArgumentException("roomId is required");
        }
        validateDates(request.checkIn(), request.checkOut());

        // 1. Réserver la chambre côté room-inventory (gRPC)
        RequestResponse response = roomInventoryClient.reserveRoom(request.roomId(), request.checkIn(), request.checkOut());
        if (!response.getSuccess()) {
            throw new RoomUnavailableException(response.getMessage());
        }

        // 2. Enregistrer la réservation dans notre base
        Booking booking = new Booking(request.guestName(), request.roomId(),
                request.checkIn(), request.checkOut(), BookingStatus.CONFIRMED);
        try {
            Booking saved = bookingRepository.save(booking);
            logger.info("Booking {} created for {} (room {})", saved.getId(), saved.getGuestName(), saved.getRoomId());
            return saved;
        } catch (RuntimeException e) {
            // Si la sauvegarde échoue, on libère la chambre pour ne pas la bloquer
            logger.error("Saving booking failed, releasing room {}", request.roomId(), e);
            roomInventoryClient.releaseRoom(request.roomId(), request.checkIn(), request.checkOut());
            throw e;
        }
    }

    @Override
    public Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Override
    public Booking cancelBooking(Long id) {
        Booking booking = getBooking(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking " + id + " is already cancelled");
        }

        RequestResponse response = roomInventoryClient.releaseRoom(booking.getRoomId(), booking.getCheckIn(), booking.getCheckOut());
        if (!response.getSuccess()) {
            throw new IllegalStateException("Could not release room: " + response.getMessage());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        logger.info("Booking {} cancelled", id);
        return saved;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("checkIn and checkOut are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkIn must be before checkOut");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("checkIn cannot be in the past");
        }
    }
}