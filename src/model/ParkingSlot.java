package model;

public class ParkingSlot {
    private final String slotNumber;
    private String slotType;
    private String zoneType; // Student, School Personnel, Visitor
    private boolean isAvailable;
    private String assignedStudentId;
    private String assignedPersonnelId;
    private String assignedVisitorId;

    public ParkingSlot(String slotNumber, String slotType, String zoneType, boolean isAvailable) {
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.zoneType = zoneType;
        this.isAvailable = isAvailable;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }

    public boolean checkAvailability() {
        return isAvailable;
    }

    public void updateAvailability(boolean available) {
        this.isAvailable = available;
    }

    public String getAssignedStudentId() {
        return assignedStudentId;
    }

    public void setAssignedStudentId(String assignedStudentId) {
        this.assignedStudentId = assignedStudentId;
    }

    public String getAssignedPersonnelId() {
        return assignedPersonnelId;
    }

    public void setAssignedPersonnelId(String assignedPersonnelId) {
        this.assignedPersonnelId = assignedPersonnelId;
    }

    public String getAssignedVisitorId() {
        return assignedVisitorId;
    }

    public void setAssignedVisitorId(String assignedVisitorId) {
        this.assignedVisitorId = assignedVisitorId;
    }

    public void clearAssignments() {
        this.assignedStudentId = null;
        this.assignedPersonnelId = null;
        this.assignedVisitorId = null;
    }

    public String getAssignedHolderLabel() {
        if (assignedStudentId != null && !assignedStudentId.isEmpty()) {
            return assignedStudentId + " (Student)";
        }
        if (assignedPersonnelId != null && !assignedPersonnelId.isEmpty()) {
            return assignedPersonnelId + " (School Personnel)";
        }
        if (assignedVisitorId != null && !assignedVisitorId.isEmpty()) {
            return assignedVisitorId + " (Visitor)";
        }
        return "-";
    }
}