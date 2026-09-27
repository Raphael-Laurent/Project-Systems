package com.bookingservice.booking.web.dto;

import com.bookingservice.booking.entity.Booking;
import com.bookingservice.booking.entity.BookingStatus;
import java.time.LocalDate;

public record BookingResponse(Long id, String guestName, Long roomId,
                              LocalDate checkIn, LocalDate checkOut, BookingStatus status) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getGuestName(), booking.getRoomId(),
                booking.getCheckIn(), booking.getCheckOut(), booking.getStatus());
    }
}