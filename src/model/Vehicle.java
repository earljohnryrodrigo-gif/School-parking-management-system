package model;

import java.time.LocalDate;

public class Vehicle {
    private final String vehicleId;
    private String plateNumber;
    private String vehicleType;
    private String stickerCategory; // Student, School Personnel, Visitor
    private String ownerId;
    private LocalDate expirationDate;

    public Vehicle(String vehicleId, String plateNumber, String vehicleType,
                   String stickerCategory, String ownerId, LocalDate expirationDate) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.stickerCategory = stickerCategory;
        this.ownerId = ownerId;
        this.expirationDate = expirationDate;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getStickerCategory() {
        return stickerCategory;
    }

    public void setStickerCategory(String stickerCategory) {
        this.stickerCategory = stickerCategory;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }
}