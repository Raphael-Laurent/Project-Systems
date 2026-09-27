
package com.bookingservice.booking.web;

import com.bookingservice.booking.service.BookingService;
import com.bookingservice.booking.web.dto.RoomDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class RoomController {

    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    private final BookingService bookingService;

    @Autowired
    public RoomController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/rooms/available")
    public List<RoomDto> getAvailableRooms(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        logger.info("GET /rooms/available {} -> {}", checkIn, checkOut);
        return bookingService.findAvailableRooms(checkIn, checkOut);
    }
}