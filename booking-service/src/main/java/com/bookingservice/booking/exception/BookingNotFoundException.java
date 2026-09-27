package com.bookingservice.booking.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Booking " + id + " not found");
    }
}