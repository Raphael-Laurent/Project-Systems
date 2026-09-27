package com.inventoryservice.inventory.config;

import com.inventoryservice.inventory.entity.Room;
import com.inventoryservice.inventory.entity.RoomType;
import com.inventoryservice.inventory.repository.RoomRepository;
import com.inventoryservice.inventory.repository.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public DataInitializer(RoomTypeRepository roomTypeRepository, RoomRepository roomRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (roomRepository.count() > 0) {
            logger.info("Rooms already present, skipping seed");
            return;
        }

        RoomType single = roomTypeRepository.save(new RoomType("SINGLE", 80.0, 1));
        RoomType dbl = roomTypeRepository.save(new RoomType("DOUBLE", 120.0, 2));
        RoomType suite = roomTypeRepository.save(new RoomType("SUITE", 250.0, 4));

        roomRepository.save(new Room("101", single));   // id 1
        roomRepository.save(new Room("102", single));   // id 2
        roomRepository.save(new Room("201", dbl));      // id 3
        roomRepository.save(new Room("202", dbl));      // id 4
        roomRepository.save(new Room("301", suite));    // id 5

        logger.info("Seeded {} room types and {} rooms", roomTypeRepository.count(), roomRepository.count());
    }
}