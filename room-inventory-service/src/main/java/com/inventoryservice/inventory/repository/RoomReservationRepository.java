package com.inventoryservice.inventory.repository;

import com.inventoryservice.inventory.entity.RoomReservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomReservationRepository extends CrudRepository<RoomReservation, Long> {

    @Query("SELECT COUNT(res) FROM RoomReservation res " +
            "WHERE res.room.id = :roomId AND res.checkIn < :checkOut AND res.checkOut > :checkIn")
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @Query("SELECT res FROM RoomReservation res " +
            "WHERE res.room.id = :roomId AND res.checkIn = :checkIn AND res.checkOut = :checkOut")
    List<RoomReservation> findExact(@Param("roomId") Long roomId,
                                    @Param("checkIn") LocalDate checkIn,
                                    @Param("checkOut") LocalDate checkOut);
}