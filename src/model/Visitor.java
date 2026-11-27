package model;

import security.PasswordHasher;

public class Visitor {
    private final String visitorId;
    private final String name;
    private final String passwordHash;
    private String requestedSlotNumber;

    public Visitor(String visitorId, String name, String password) {
        this.visitorId = visitorId;
        this.name = name;
        this.passwordHash = PasswordHasher.hash(password);
        this.requestedSlotNumber = null;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return passwordHash;
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
