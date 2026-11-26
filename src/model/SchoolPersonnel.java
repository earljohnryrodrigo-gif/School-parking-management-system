package model;

public class SchoolPersonnel {
    private final String personnelId;
    private final String name;
    private final String password;
    private String assignedSlotNumber;
    private String reservedSlotNumber;

    public SchoolPersonnel(String personnelId, String name, String password) {
        this.personnelId = personnelId;
        this.name = name;
        this.password = password;
        this.assignedSlotNumber = null;
        this.reservedSlotNumber = null;
    }

    public String getPersonnelId() {
        return personnelId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String viewAssignedSlot() {
        return assignedSlotNumber;
    }

    public void setAssignedSlotNumber(String assignedSlotNumber) {
        this.assignedSlotNumber = assignedSlotNumber;
    }

    public boolean selectSlot(ParkingSlot slot) {
        if (assignedSlotNumber != null || slot == null) {
            return false;
        }
        if (!"School Personnel".equalsIgnoreCase(slot.getZoneType()) || !slot.checkAvailability()) {
            return false;
        }
        slot.updateAvailability(false);
        slot.setAssignedPersonnelId(personnelId);
        this.assignedSlotNumber = slot.getSlotNumber();
        return true;
    }

    public String viewReservedSlot() {
        return reservedSlotNumber;
    }

    public boolean reservedSlot(String slotNumber) {
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            return false;
        }
        this.reservedSlotNumber = slotNumber.trim();
        return true;
    }

    public void clearReservedSlot() {
        this.reservedSlotNumber = null;
    }
}