package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingRecord {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String recordId;
    private final String vehicleId;
    private final String slotNumber;
    private final String visitorId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean surrenderedVisitorId;

    public ParkingRecord(String recordId, String vehicleId, String slotNumber, String visitorId) {
        this.recordId = recordId;
        this.vehicleId = vehicleId;
        this.slotNumber = slotNumber;
        this.visitorId = visitorId;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.surrenderedVisitorId = false;
    }

    /** Returns a detached view so callers cannot change stored entry and exit times. */
    public ParkingRecord copy() {
        ParkingRecord copy = new ParkingRecord(recordId, vehicleId, slotNumber, visitorId);
        copy.entryTime = entryTime;
        copy.exitTime = exitTime;
        copy.surrenderedVisitorId = surrenderedVisitorId;
        return copy;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public String getFormattedEntryTime() {
        return entryTime != null ? entryTime.format(FORMATTER) : "-";
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(FORMATTER) : "Active";
    }

    public boolean isSurrenderedVisitorId() {
        return surrenderedVisitorId;
    }

    public void recordEntry() {
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
    }

    public void recordExit() {
        this.exitTime = LocalDateTime.now();
    }

    public void markVisitorIdSurrendered(boolean surrendered) {
        this.surrenderedVisitorId = surrendered;
    }
}
