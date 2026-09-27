package com.bookingservice.booking.repository;

import com.bookingservice.booking.entity.Booking;
import org.springframework.data.repository.CrudRepository;

public interface BookingRepository extends CrudRepository<Booking, Long> {
}