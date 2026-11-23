package model;

public class Reservation {
    private final String reservationId;
    private final String requesterId;
    private final String requesterName;
    private final UserType requesterRole;
    private final String slotNumber;
    private String startDateTime;
    private String endDateTime;
    private String status; // Pending, Approved, Rejected

    public Reservation(String reservationId, String requesterId, String requesterName,
                       UserType requesterRole, String slotNumber,
                       String startDateTime, String endDateTime, String status) {
        this.reservationId = reservationId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterRole = requesterRole;
        this.slotNumber = slotNumber;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public UserType getRequesterRole() {
        return requesterRole;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void approve() {
        this.status = "Approved";
    }

    public void reject() {
        this.status = "Rejected";
    }
}