package model;

import java.time.LocalDate;

public class Vehicle {
    private final String vehicleId;
    private String plateNumber;
    private String vehicleType;
    private String stickerCategory; // Student, School Personnel, Visitor
    private String ownerId;
    private LocalDate expirationDate;
    private String registrationStatus;
    private LocalDate approvalDate;
    private String rejectionReason;
    private String paymentStatus;

    public Vehicle(String vehicleId, String plateNumber, String vehicleType,
                   String stickerCategory, String ownerId, LocalDate expirationDate) {
        this(vehicleId, plateNumber, vehicleType, stickerCategory, ownerId, expirationDate,
                "APPROVED", LocalDate.now(), "", "NOT_REQUIRED");
    }

    public Vehicle(String vehicleId, String plateNumber, String vehicleType, String stickerCategory,
                   String ownerId, LocalDate expirationDate, String registrationStatus,
                   LocalDate approvalDate, String rejectionReason, String paymentStatus) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.stickerCategory = stickerCategory;
        this.ownerId = ownerId;
        this.expirationDate = expirationDate;
        this.registrationStatus = registrationStatus == null ? "PENDING" : registrationStatus;
        this.approvalDate = approvalDate;
        this.rejectionReason = rejectionReason == null ? "" : rejectionReason;
        this.paymentStatus = paymentStatus == null ? "NOT_REQUIRED" : paymentStatus;
    }

    /** Returns a detached view so callers cannot change stored vehicle details directly. */
    public Vehicle copy() {
        return new Vehicle(vehicleId, plateNumber, vehicleType, stickerCategory, ownerId, expirationDate,
                registrationStatus, approvalDate, rejectionReason, paymentStatus);
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

    public String getRegistrationStatus() {
        if ("APPROVED".equalsIgnoreCase(registrationStatus) && isExpired()) return "EXPIRED";
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason == null ? "" : rejectionReason; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus == null ? "NOT_REQUIRED" : paymentStatus; }

    public boolean isApprovedAndValid() {
        return "APPROVED".equalsIgnoreCase(registrationStatus)
                && expirationDate != null && !expirationDate.isBefore(LocalDate.now());
    }

    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now());
    }
}
