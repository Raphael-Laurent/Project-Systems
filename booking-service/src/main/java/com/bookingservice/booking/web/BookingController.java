package com.bookingservice.booking.web;

import com.bookingservice.booking.entity.Booking;
import com.bookingservice.booking.service.BookingService;
import com.bookingservice.booking.web.dto.BookingResponse;
import com.bookingservice.booking.web.dto.CreateBookingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        logger.info("POST /bookings room={} guest={}", request.roomId(), request.guestName());
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity
                .created(URI.create("/bookings/" + booking.getId()))
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable("id") Long id) {
        logger.info("GET /bookings/{}", id);
        return BookingResponse.from(bookingService.getBooking(id));
    }

    @DeleteMapping("/{id}")
    public BookingResponse cancelBooking(@PathVariable("id") Long id) {
        logger.info("DELETE /bookings/{}", id);
        return BookingResponse.from(bookingService.cancelBooking(id));
    }
}