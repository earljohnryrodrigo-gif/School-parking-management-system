package model;

public class Visitor {
    private final String visitorId;
    private final String name;
    private final String password;
    private String requestedSlotNumber;

    public Visitor(String visitorId, String name, String password) {
        this.visitorId = visitorId;
        this.name = name;
        this.password = password;
        this.requestedSlotNumber = null;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getRequestedSlotNumber() {
        return requestedSlotNumber;
    }

    public boolean requestReservation(String slotNumber) {
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            return false;
        }
        this.requestedSlotNumber = slotNumber.trim();
        return true;
    }

    public void clearRequestedSlot() {
        this.requestedSlotNumber = null;
    }
}