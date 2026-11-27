package model;

import java.time.LocalDateTime;

/** Reminder sent to an owner and visible to the administrator. */
public final class VehicleReminder {
    private final String vehicleId;
    private final String ownerId;
    private final String plateNumber;
    private final String message;
    private final LocalDateTime createdAt;
    private final boolean manual;

    public VehicleReminder(String vehicleId, String ownerId, String plateNumber, String message, boolean manual) {
        this.vehicleId = vehicleId;
        this.ownerId = ownerId;
        this.plateNumber = plateNumber;
        this.message = message;
        this.manual = manual;
        this.createdAt = LocalDateTime.now();
    }

    public String getVehicleId() { return vehicleId; }
    public String getOwnerId() { return ownerId; }
    public String getPlateNumber() { return plateNumber; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isManual() { return manual; }
}
