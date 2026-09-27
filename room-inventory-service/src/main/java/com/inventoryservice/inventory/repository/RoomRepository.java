package com.inventoryservice.inventory.repository;

import com.inventoryservice.inventory.entity.Room;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Long> {

    // Chambres sans aucune réservation qui chevauche [checkIn, checkOut[
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
            "SELECT res.room.id FROM RoomReservation res " +
            "WHERE res.checkIn < :checkOut AND res.checkOut > :checkIn)")
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut);
}