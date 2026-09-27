package com.bookingservice.booking.web.dto;

import java.time.LocalDate;

public record CreateBookingRequest(String guestName, Long roomId, LocalDate checkIn, LocalDate checkOut) {
}