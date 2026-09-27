package com.inventoryservice.inventory.service;

import com.inventoryservice.inventory.entity.Room;
import com.inventoryservice.inventory.entity.RoomReservation;
import com.inventoryservice.inventory.exception.ReservationNotFoundException;
import com.inventoryservice.inventory.exception.RoomNotFoundException;
import com.inventoryservice.inventory.exception.RoomUnavailableException;
import com.inventoryservice.inventory.repository.RoomRepository;
import com.inventoryservice.inventory.repository.RoomReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RoomInventoryServiceImpl implements RoomInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(RoomInventoryServiceImpl.class);

    private final RoomRepository roomRepository;
    private final RoomReservationRepository reservationRepository;

    @Autowired
    public RoomInventoryServiceImpl(RoomRepository roomRepository, RoomReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        List<Room> rooms = roomRepository.findAvailableRooms(checkIn, checkOut);
        logger.info("{} room(s) available from {} to {}", rooms.size(), checkIn, checkOut);
        return rooms;
    }

    @Override
    @Transactional
    public void reserveRoom(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        boolean alreadyBooked = reservationRepository.countOverlapping(roomId, checkIn, checkOut) > 0;
        if (alreadyBooked) {
            throw new RoomUnavailableException("Room " + roomId + " is already booked between " + checkIn + " and " + checkOut);
        }

        reservationRepository.save(new RoomReservation(room, checkIn, checkOut));
        logger.info("Room {} reserved from {} to {}", roomId, checkIn, checkOut);
    }

    @Override
    @Transactional
    public void releaseRoom(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<RoomReservation> reservations = reservationRepository.findExact(roomId, checkIn, checkOut);
        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException(
                    "No reservation for room " + roomId + " from " + checkIn + " to " + checkOut);
        }
        RoomReservation reservation = reservations.get(0);

        reservationRepository.delete(reservation);
        logger.info("Room {} released from {} to {}", roomId, checkIn, checkOut);
    }

    // Prix total du séjour = nombre de nuits x prix par nuit du type de chambre
    @Override
    public double computePrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return nights * room.getType().getPricePerNight();
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("checkIn and checkOut are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("checkIn must be before checkOut");
        }
    }
}