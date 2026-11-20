package com.example.collector;

import jakarta.persistence.*;

@Entity
public class Collector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phoneNumber;
    private String vehicleNumber;
    private boolean available = true;
    @Version
    private long version;

    protected Collector() {
    }

    public Collector(String name, String phone, String vehicle) {
        this.name = name;
        this.phoneNumber = phone;
        this.vehicleNumber = vehicle;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean value) {
        available = value;
    }

    public void update(String name, String phone, String vehicle) {
        this.name = name;
        this.phoneNumber = phone;
        this.vehicleNumber = vehicle;
    }
}
