package com.inventoryservice.inventory.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("Room " + roomId + " not found");
    }
}