package model;

import java.time.LocalDateTime;
import java.util.Arrays;

/** One registration or renewal request awaiting administrator review. */
public class VehicleApplication {
    private final String applicationId;
    private final String vehicleId;
    private final String ownerId;
    private final String applicationType;
    private LocalDateTime submittedAt;
    private final int feeAmount;
    private String applicantName;
    private String identificationNumber;
    private String programOrDepartment;
    private String vehicleType;
    private String plateNumber;
    private String status = "PENDING";
    private String rejectionReason = "";
    private String paymentStatus;
    private String paymentMethod = "";
    private String paymentReference = "";
    private byte[] receiptImage;
    private String paymentRejectionReason = "";

    public VehicleApplication(String applicationId, String vehicleId, String ownerId, String applicationType,
                              String applicantName, String identificationNumber, String programOrDepartment,
                              String vehicleType, String plateNumber, int feeAmount) {
        this.applicationId = applicationId;
        this.vehicleId = vehicleId;
        this.ownerId = ownerId;
        this.applicationType = applicationType;
        this.applicantName = applicantName;
        this.identificationNumber = identificationNumber;
        this.programOrDepartment = programOrDepartment;
        this.vehicleType = vehicleType;
        this.plateNumber = plateNumber;
        this.feeAmount = feeAmount;
        this.submittedAt = LocalDateTime.now();
        this.paymentStatus = feeAmount == 0 ? "NOT_REQUIRED" : "NOT_SUBMITTED";
    }

    private VehicleApplication(VehicleApplication source) {
        this(source.applicationId, source.vehicleId, source.ownerId, source.applicationType,
                source.applicantName, source.identificationNumber, source.programOrDepartment,
                source.vehicleType, source.plateNumber, source.feeAmount);
        this.submittedAt = source.submittedAt;
        this.status = source.status;
        this.rejectionReason = source.rejectionReason;
        this.paymentStatus = source.paymentStatus;
        this.paymentMethod = source.paymentMethod;
        this.paymentReference = source.paymentReference;
        this.receiptImage = source.receiptImage == null ? null : Arrays.copyOf(source.receiptImage, source.receiptImage.length);
        this.paymentRejectionReason = source.paymentRejectionReason;
    }

    public VehicleApplication copy() { return new VehicleApplication(this); }
    public String getApplicationId() { return applicationId; }
    public String getVehicleId() { return vehicleId; }
    public String getOwnerId() { return ownerId; }
    public String getApplicationType() { return applicationType; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public int getFeeAmount() { return feeAmount; }
    public String getApplicantName() { return applicantName; }
    public String getIdentificationNumber() { return identificationNumber; }
    public String getProgramOrDepartment() { return programOrDepartment; }
    public String getVehicleType() { return vehicleType; }
    public String getPlateNumber() { return plateNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String reason) { this.rejectionReason = reason == null ? "" : reason; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String status) { this.paymentStatus = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPayment(String method, String reference, byte[] image) {
        this.paymentMethod = method;
        this.paymentReference = reference;
        this.receiptImage = image == null ? null : Arrays.copyOf(image, image.length);
        this.paymentStatus = "PENDING_VERIFICATION";
        this.paymentRejectionReason = "";
    }
    public byte[] getReceiptImage() { return receiptImage == null ? null : Arrays.copyOf(receiptImage, receiptImage.length); }
    public String getPaymentRejectionReason() { return paymentRejectionReason; }
    public void setPaymentRejectionReason(String reason) { this.paymentRejectionReason = reason == null ? "" : reason; }
}
