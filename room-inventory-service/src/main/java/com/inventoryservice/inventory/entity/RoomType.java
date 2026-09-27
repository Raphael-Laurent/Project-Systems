package com.inventoryservice.inventory.entity;

import jakarta.persistence.*;

@Entity
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // SINGLE, DOUBLE, SUITE
    private double pricePerNight;
    private int capacity;

    public RoomType() {
    }

    public RoomType(String name, double pricePerNight, int capacity) {
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}