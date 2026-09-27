// booking-service/src/main/java/com/bookingservice/booking/service/BookingService.java
package com.bookingservice.booking.service;

import com.bookingservice.booking.entity.Booking;
import com.bookingservice.booking.web.dto.CreateBookingRequest;
import com.bookingservice.booking.web.dto.RoomDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    List<RoomDto> findAvailableRooms(LocalDate checkIn, LocalDate checkOut);
    Booking createBooking(CreateBookingRequest request);
    Booking getBooking(Long id);
    Booking cancelBooking(Long id);
}