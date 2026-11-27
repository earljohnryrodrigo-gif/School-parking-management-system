package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A visitor request or a School Personnel parking reservation. */
public class Reservation {
    private final String reservationId;
    private final String requesterId;
    private final String requesterName;
    private final UserType requesterRole;
    private String slotNumber;
    private String startDateTime;
    private String endDateTime;
    private String status;
    private final String vehicleId;
    private final String plateNumber;
    private final String vehicleType;
    private final String visitPurpose;
    private final LocalDate requestedDate;
    private String rejectionReason = "";
    private LocalDateTime arrivedAt;
    private LocalDateTime departedAt;
    private LocalDateTime noShowAt;

    /** Existing constructor retained for School Personnel and seeded reservation compatibility. */
    public Reservation(String reservationId, String requesterId, String requesterName,
                       UserType requesterRole, String slotNumber,
                       String startDateTime, String endDateTime, String status) {
        this(reservationId, requesterId, requesterName, requesterRole, slotNumber,
                startDateTime, endDateTime, status, null, null, null, null, null,
                null, null, null, "");
    }

    public Reservation(String reservationId, String requesterId, String requesterName,
                       UserType requesterRole, String slotNumber,
                       String startDateTime, String endDateTime, String status,
                       String vehicleId, String plateNumber, String vehicleType,
                       String visitPurpose, LocalDate requestedDate) {
        this(reservationId, requesterId, requesterName, requesterRole, slotNumber,
                startDateTime, endDateTime, status, vehicleId, plateNumber, vehicleType,
                visitPurpose, requestedDate, null, null, null, "");
    }

    private Reservation(String reservationId, String requesterId, String requesterName,
                        UserType requesterRole, String slotNumber,
                        String startDateTime, String endDateTime, String status,
                        String vehicleId, String plateNumber, String vehicleType,
                        String visitPurpose, LocalDate requestedDate,
                        LocalDateTime arrivedAt, LocalDateTime departedAt,
                        LocalDateTime noShowAt, String rejectionReason) {
        this.reservationId = reservationId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterRole = requesterRole;
        this.slotNumber = slotNumber;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.visitPurpose = visitPurpose;
        this.requestedDate = requestedDate;
        this.arrivedAt = arrivedAt;
        this.departedAt = departedAt;
        this.noShowAt = noShowAt;
        this.rejectionReason = rejectionReason == null ? "" : rejectionReason;
    }

    /** Returns a detached view so callers cannot change stored reservation state. */
    public Reservation copy() {
        return new Reservation(reservationId, requesterId, requesterName, requesterRole,
                slotNumber, startDateTime, endDateTime, status, vehicleId, plateNumber,
                vehicleType, visitPurpose, requestedDate, arrivedAt, departedAt,
                noShowAt, rejectionReason);
    }

    public String getReservationId() { return reservationId; }
    public String getRequesterId() { return requesterId; }
    public String getRequesterName() { return requesterName; }
    public UserType getRequesterRole() { return requesterRole; }
    public String getSlotNumber() { return slotNumber; }
    public String getStartDateTime() { return startDateTime; }
    public String getEndDateTime() { return endDateTime; }
    public String getStatus() { return status; }
    public String getVehicleId() { return vehicleId; }
    public String getPlateNumber() { return plateNumber; }
    public String getVehicleType() { return vehicleType; }
    public String getVisitPurpose() { return visitPurpose; }
    public LocalDate getRequestedDate() { return requestedDate; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getArrivedAt() { return arrivedAt; }
    public LocalDateTime getDepartedAt() { return departedAt; }
    public LocalDateTime getNoShowAt() { return noShowAt; }

    public void assignSlotAndTime(String slotNumber, String startDateTime, String endDateTime) {
        this.slotNumber = slotNumber;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public void setStartDateTime(String startDateTime) { this.startDateTime = startDateTime; }
    public void setEndDateTime(String endDateTime) { this.endDateTime = endDateTime; }
    public void approve() { this.status = "Approved"; }
    public void reject(String reason) { this.status = "Rejected"; this.rejectionReason = reason == null ? "" : reason.trim(); }
    public void reject() { reject(""); }
    public void markArrived(LocalDateTime time) { this.status = "Arrived"; this.arrivedAt = time; }
    public void markDeparted(LocalDateTime time) { this.status = "Departed"; this.departedAt = time; }
    public void markNoShow(LocalDateTime time) { this.status = "No-Show"; this.noShowAt = time; }
    public void complete() { this.status = "Completed"; }
    public void cancel() { this.status = "Cancelled"; }
}
