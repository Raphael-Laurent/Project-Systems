package com.inventoryservice.inventory.service;

import com.inventoryservice.inventory.entity.Room;

import java.time.LocalDate;
import java.util.List;

public interface RoomInventoryService {
    List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut);
    void reserveRoom(Long roomId, LocalDate checkIn, LocalDate checkOut);
    void releaseRoom(Long roomId, LocalDate checkIn, LocalDate checkOut);
    double computePrice(Room room, LocalDate checkIn, LocalDate checkOut);
}