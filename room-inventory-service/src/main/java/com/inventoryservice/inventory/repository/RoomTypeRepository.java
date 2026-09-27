package com.inventoryservice.inventory.repository;

import com.inventoryservice.inventory.entity.RoomType;
import org.springframework.data.repository.CrudRepository;

public interface RoomTypeRepository extends CrudRepository<RoomType, Long> {
}