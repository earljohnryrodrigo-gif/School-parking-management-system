package ui;

import model.UserType;
import model.Vehicle;
import model.VehicleApplication;
import service.ParkingService;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Registration, renewal, and administrator review screens for vehicle records. */
public final class VehicleManagementPanel extends JPanel {
    private static final String[] FILTERS = {"All", "Pending", "Approved", "Rejected", "Expired", "Payment Verification", "Expiring Soon"};
    private static final int MAX_RECEIPT_BYTES = 10 * 1024 * 1024;

    private final ParkingService service;
    private final Runnable refreshDashboard;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Vehicle ID", "Plate Number", "Vehicle Type", "Sticker Category", "Owner", "Status",
            "Approval Date", "Expiration Date", "Payment Status", "Administrator Note"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> filterBox = new JComboBox<>(FILTERS);
    private final JPanel adminActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel ownerActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JButton approveButton = Theme.primaryButton("Approve Registration / Renewal");
    private final JButton rejectButton = Theme.secondaryButton("Reject Registration");
    private final JButton verifyPaymentButton = Theme.primaryButton("Verify Payment");
    private final JButton rejectPaymentButton = Theme.secondaryButton("Reject Payment");
    private final JButton proofButton = Theme.secondaryButton("View Payment Proof");
    private final JButton remindButton = Theme.secondaryButton("Remind Expiring Owner");
    private final JButton registerButton = Theme.primaryButton("+ Register Vehicle");
    private final JButton renewButton = Theme.secondaryButton("Renew Selected");
    private final JButton submitPaymentButton = Theme.secondaryButton("Submit / Resubmit Payment");
    private final JButton adminRefreshButton = Theme.secondaryButton("Refresh");
    private final JButton ownerRefreshButton = Theme.secondaryButton("Refresh");

    public VehicleManagementPanel(ParkingService service, Runnable refreshDashboard) {
        this.service = service;
        this.refreshDashboard = refreshDashboard;
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setOpaque(false);
        buildAdminActions();
        buildOwnerActions();
        toolbar.add(adminActions);
        toolbar.add(ownerActions);
        add(toolbar, BorderLayout.NORTH);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(table);
        table.setDefaultRenderer(Object.class, new VehicleCellRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateActionStates();
        });
        JScrollPane scroll = new JScrollPane(table);
        Theme.styleScrollPane(scroll);
        add(scroll, BorderLayout.CENTER);
        updateActionStates();
    }

    private void buildAdminActions() {
        adminActions.setOpaque(false);
        JLabel filterLabel = new JLabel("View:");
        filterLabel.setForeground(Theme.TEXT_MUTED);
        filterBox.setFont(Theme.FONT_LABEL);
        filterBox.addActionListener(e -> refreshForCurrentUser());
        adminActions.add(filterLabel);
        adminActions.add(filterBox);
        adminActions.add(approveButton);
        adminActions.add(rejectButton);
        adminActions.add(verifyPaymentButton);
        adminActions.add(rejectPaymentButton);
        adminActions.add(proofButton);
        adminActions.add(remindButton);
        adminActions.add(adminRefreshButton);
        approveButton.addActionListener(e -> approveSelected());
        rejectButton.addActionListener(e -> rejectSelected());
        verifyPaymentButton.addActionListener(e -> verifySelectedPayment());
        rejectPaymentButton.addActionListener(e -> rejectSelectedPayment());
        proofButton.addActionListener(e -> showSelectedProof());
        remindButton.addActionListener(e -> remindSelectedOwner());
        adminRefreshButton.addActionListener(e -> refreshDashboard.run());
    }

    private void buildOwnerActions() {
        ownerActions.setOpaque(false);
        ownerActions.add(registerButton);
        ownerActions.add(renewButton);
        ownerActions.add(submitPaymentButton);
        ownerActions.add(ownerRefreshButton);
        registerButton.addActionListener(e -> openRegistrationForm());
        renewButton.addActionListener(e -> renewSelected());
        submitPaymentButton.addActionListener(e -> submitPaymentForSelected());
        ownerRefreshButton.addActionListener(e -> refreshDashboard.run());
    }

    public void openRegistrationForm() {
        ParkingService.UserAccount user = service.getCurrentUser();
        if (user == null || (user.getUserType() != UserType.STUDENT && user.getUserType() != UserType.SCHOOL_PERSONNEL)) {
            JOptionPane.showMessageDialog(this, "Vehicle registration is available to Students and School Personnel.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField fullName = new JTextField(user.getFullName(), 22);
        JTextField identity = new JTextField(user.getUserType() == UserType.STUDENT ? user.getStudentId() : "", 22);
        JTextField program = new JTextField(user.getUserType() == UserType.STUDENT ? user.getCourseYear() : "", 22);
        JComboBox<String> type = new JComboBox<>(new String[]{"Sedan", "SUV", "Motorcycle", "Van"});
        JTextField plate = new JTextField(22);
        String identityLabel = user.getUserType() == UserType.STUDENT ? "Student ID" : "Personnel/Employee ID";
        String programLabel = user.getUserType() == UserType.STUDENT ? "Program" : "Department";
        JPanel form = applicationForm(fullName, identity, program, type, plate, identityLabel, programLabel);
        JLabel feeLabel = new JLabel("Registration fee: " + peso(service.getVehicleRegistrationFee()));
        feeLabel.setFont(Theme.FONT_LABEL_BOLD);
        form.add(feeLabel);
        plate.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFee() { feeLabel.setText("Registration fee: " + peso(service.getVehicleRegistrationFee(plate.getText()))); }
            @Override public void insertUpdate(DocumentEvent e) { updateFee(); }
            @Override public void removeUpdate(DocumentEvent e) { updateFee(); }
            @Override public void changedUpdate(DocumentEvent e) { updateFee(); }
        });

        if (JOptionPane.showConfirmDialog(this, form, "Vehicle Registration", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        int registrationFee = service.getVehicleRegistrationFee(plate.getText());
        String result = service.submitVehicleRegistration(fullName.getText(), identity.getText(), program.getText(),
                String.valueOf(type.getSelectedItem()), plate.getText());
        if (!"OK".equals(result)) {
            JOptionPane.showMessageDialog(this, result, "Registration Not Submitted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshDashboard.run();
        JOptionPane.showMessageDialog(this,
                "Your registration is PENDING administrator approval. "
                        + (registrationFee > 0
                        ? "Submit the payment proof from My Vehicles before approval." : "This first vehicle registration is FREE."),
                "Registration Submitted", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showPaymentVerification() {
        filterBox.setSelectedItem("Payment Verification");
    }

    private JPanel applicationForm(JTextField fullName, JTextField identity, JTextField program,
                                   JComboBox<String> type, JTextField plate, String identityLabel, String programLabel) {
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBackground(Theme.CARD_WHITE);
        form.add(new JLabel("Full Name")); form.add(fullName);
        form.add(new JLabel(identityLabel)); form.add(identity);
        form.add(new JLabel(programLabel)); form.add(program);
        form.add(new JLabel("Vehicle Type")); form.add(type);
        form.add(new JLabel("Plate Number")); form.add(plate);
        for (Component component : form.getComponents()) {
            if (component instanceof JTextField) Theme.styleInput((JTextField) component);
        }
        return form;
    }

    private void renewSelected() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this, "Select an approved vehicle to renew.");
            return;
        }
        int fee = service.getVehicleRenewalFee(vehicle.getVehicleId());
        if (fee < 0) {
            JOptionPane.showMessageDialog(this, "Only your approved or expired vehicles can be renewed.", "Renewal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ParkingService.UserAccount user = service.getCurrentUser();
        JTextField fullName = new JTextField(user.getFullName(), 22);
        JTextField identity = new JTextField(user.getUserType() == UserType.STUDENT ? user.getStudentId() : "", 22);
        JTextField program = new JTextField(user.getUserType() == UserType.STUDENT ? user.getCourseYear() : "", 22);
        JComboBox<String> type = new JComboBox<>(new String[]{"Sedan", "SUV", "Motorcycle", "Van"});
        type.setSelectedItem(vehicle.getVehicleType());
        JTextField plate = new JTextField(vehicle.getPlateNumber(), 22);
        String identityLabel = user.getUserType() == UserType.STUDENT ? "Student ID" : "Personnel/Employee ID";
        String programLabel = user.getUserType() == UserType.STUDENT ? "Program" : "Department";
        JPanel form = applicationForm(fullName, identity, program, type, plate, identityLabel, programLabel);
        JLabel feeLabel = new JLabel("Renewal fee: " + peso(fee)
                + (fee == 0 ? " (early renewal is FREE)" : " (expired renewal)"));
        feeLabel.setFont(Theme.FONT_LABEL_BOLD);
        form.add(feeLabel);
        if (JOptionPane.showConfirmDialog(this, form, "Vehicle Renewal", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String result = service.submitVehicleRenewal(vehicle.getVehicleId(), fullName.getText(), identity.getText(),
                program.getText(), String.valueOf(type.getSelectedItem()), plate.getText());
        if (!"OK".equals(result)) {
            JOptionPane.showMessageDialog(this, result, "Renewal Not Submitted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshDashboard.run();
        JOptionPane.showMessageDialog(this,
                "Your renewal is PENDING administrator approval. "
                        + (fee > 0 ? "Submit the payment proof before approval." : "Your current approval remains valid until its expiration date."),
                "Renewal Submitted", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitPaymentForSelected() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this, "Select a vehicle with a pending paid application.");
            return;
        }
        VehicleApplication application = service.getLatestVehicleApplication(vehicle.getVehicleId());
        if (application == null || !"PENDING".equalsIgnoreCase(application.getStatus()) || application.getFeeAmount() <= 0) {
            JOptionPane.showMessageDialog(this, "There is no pending payment for this vehicle.", "Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> method = new JComboBox<>(new String[]{"GCash", "Maya", "Bank Transfer", "Cash/Cashier"});
        JTextField reference = new JTextField(22);
        JLabel receiptLabel = new JLabel("No receipt selected");
        JButton chooseReceipt = Theme.secondaryButton("Choose Receipt Image");
        final File[] receipt = {null};
        chooseReceipt.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Receipt images (PNG/JPG)", "png", "jpg", "jpeg"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                receipt[0] = chooser.getSelectedFile();
                receiptLabel.setText(receipt[0].getName());
            }
        });
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBackground(Theme.CARD_WHITE);
        form.add(new JLabel("Amount due")); form.add(new JLabel(peso(application.getFeeAmount())));
        form.add(new JLabel("Payment Method")); form.add(method);
        form.add(new JLabel("Reference Number / OR Number")); form.add(reference);
        JPanel receiptRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        receiptRow.setOpaque(false); receiptRow.add(chooseReceipt); receiptRow.add(receiptLabel);
        JPanel container = new JPanel(new BorderLayout(0, 8)); container.setBackground(Theme.CARD_WHITE);
        container.add(form, BorderLayout.CENTER); container.add(receiptRow, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, container, "Submit Payment Proof", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        if (receipt[0] == null) {
            JOptionPane.showMessageDialog(this, "Choose a receipt image before submitting.", "Receipt Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (Files.size(receipt[0].toPath()) > MAX_RECEIPT_BYTES) {
                JOptionPane.showMessageDialog(this, "The receipt image must be 10 MB or smaller.", "Image Too Large", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String result = service.submitVehiclePayment(application.getApplicationId(), String.valueOf(method.getSelectedItem()),
                    reference.getText(), Files.readAllBytes(receipt[0].toPath()));
            if (!"OK".equals(result)) {
                JOptionPane.showMessageDialog(this, result, "Payment Not Submitted", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refreshDashboard.run();
            JOptionPane.showMessageDialog(this, "Payment proof submitted. The Administrator must verify it before approval.",
                    "Payment Submitted", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not read the selected receipt image.", "Payment Not Submitted", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveSelected() {
        VehicleApplication application = selectedApplication();
        if (application == null) return;
        String result = service.approveVehicleApplication(application.getApplicationId());
        if ("OK".equals(result)) {
            refreshDashboard.run();
            JOptionPane.showMessageDialog(this, "Vehicle approved for five months from today.", "Approved", JOptionPane.INFORMATION_MESSAGE);
        } else JOptionPane.showMessageDialog(this, result, "Approval Not Completed", JOptionPane.WARNING_MESSAGE);
    }

    private void rejectSelected() {
        VehicleApplication application = selectedApplication();
        if (application == null) return;
        String reason = JOptionPane.showInputDialog(this, "Enter the rejection reason:", "Reject Vehicle Application", JOptionPane.QUESTION_MESSAGE);
        if (reason == null) return;
        String result = service.rejectVehicleApplication(application.getApplicationId(), reason);
        if ("OK".equals(result)) refreshDashboard.run();
        else JOptionPane.showMessageDialog(this, result, "Rejection Not Completed", JOptionPane.WARNING_MESSAGE);
    }

    private void verifySelectedPayment() {
        VehicleApplication application = selectedApplication();
        if (application == null) return;
        String result = service.verifyVehiclePayment(application.getApplicationId());
        if ("OK".equals(result)) {
            refreshDashboard.run();
            JOptionPane.showMessageDialog(this, "Payment marked VERIFIED. Registration still requires separate approval.",
                    "Payment Verified", JOptionPane.INFORMATION_MESSAGE);
        } else JOptionPane.showMessageDialog(this, result, "Verification Not Completed", JOptionPane.WARNING_MESSAGE);
    }

    private void rejectSelectedPayment() {
        VehicleApplication application = selectedApplication();
        if (application == null) return;
        String reason = JOptionPane.showInputDialog(this, "Enter why this payment could not be verified:", "Reject Payment", JOptionPane.QUESTION_MESSAGE);
        if (reason == null) return;
        String result = service.rejectVehiclePayment(application.getApplicationId(), reason);
        if ("OK".equals(result)) refreshDashboard.run();
        else JOptionPane.showMessageDialog(this, result, "Payment Rejection Not Completed", JOptionPane.WARNING_MESSAGE);
    }

    private void showSelectedProof() {
        VehicleApplication application = selectedApplication();
        if (application == null) return;
        byte[] proof = service.getVehiclePaymentProof(application.getApplicationId());
        if (proof == null) {
            JOptionPane.showMessageDialog(this, "No receipt image is attached to this application.", "Payment Proof", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Image image = ImageIO.read(new java.io.ByteArrayInputStream(proof));
            if (image == null) throw new IllegalArgumentException("Unsupported image");
            Image scaled = image.getScaledInstance(Math.min(760, image.getWidth(null)), -1, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new javax.swing.ImageIcon(scaled), SwingConstants.CENTER);
            JPanel preview = new JPanel(new BorderLayout(8, 8));
            preview.add(new JLabel(application.getPaymentMethod() + "  |  " + application.getPaymentReference()), BorderLayout.NORTH);
            preview.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, preview, "Submitted Payment Proof", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "The stored receipt image cannot be displayed.", "Payment Proof", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remindSelectedOwner() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) return;
        String result = service.remindVehicleExpiry(vehicle.getVehicleId());
        if ("OK".equals(result)) {
            refreshDashboard.run();
            JOptionPane.showMessageDialog(this, "Expiration reminder sent. It will appear as a red banner on the owner's dashboard.",
                    "Reminder Sent", JOptionPane.INFORMATION_MESSAGE);
        } else JOptionPane.showMessageDialog(this, result, "Reminder Not Sent", JOptionPane.WARNING_MESSAGE);
    }

    private Vehicle selectedVehicle() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return findVehicle(model.getValueAt(row, 0).toString());
    }

    private Vehicle findVehicle(String vehicleId) {
        for (Vehicle vehicle : service.getVehicles()) if (vehicle.getVehicleId().equals(vehicleId)) return vehicle;
        return null;
    }

    private VehicleApplication selectedApplication() {
        Vehicle vehicle = selectedVehicle();
        if (vehicle == null) {
            JOptionPane.showMessageDialog(this, "Select a vehicle row first.");
            return null;
        }
        VehicleApplication application = service.getLatestVehicleApplication(vehicle.getVehicleId());
        if (application == null) JOptionPane.showMessageDialog(this, "There is no registration or renewal application for this vehicle.");
        return application;
    }

    private String peso(int amount) { return amount == 0 ? "FREE" : "\u20B1" + String.format("%,d", amount); }

    public void refreshForCurrentUser() {
        ParkingService.UserAccount user = service.getCurrentUser();
        boolean admin = user != null && user.getUserType() == UserType.PARKING_ADMINISTRATOR;
        boolean owner = user != null && (user.getUserType() == UserType.STUDENT || user.getUserType() == UserType.SCHOOL_PERSONNEL);
        adminActions.setVisible(admin);
        ownerActions.setVisible(owner);
        model.setRowCount(0);
        if (user == null || user.getUserType() == UserType.VISITOR) {
            updateActionStates();
            return;
        }
        List<Vehicle> vehicles = service.getVehiclesForCurrentUser();
        for (Vehicle vehicle : vehicles) {
            VehicleApplication latest = service.getLatestVehicleApplication(vehicle.getVehicleId());
            if (admin && !matchesFilter(vehicle, latest, String.valueOf(filterBox.getSelectedItem()))) continue;
            String status = vehicle.getRegistrationStatus();
            if (latest != null && "RENEWAL".equals(latest.getApplicationType())
                    && "PENDING".equalsIgnoreCase(latest.getStatus()) && "APPROVED".equalsIgnoreCase(status)) {
                status = "APPROVED - RENEWAL PENDING";
            }
            String paymentStatus = latest == null ? vehicle.getPaymentStatus() : latest.getPaymentStatus();
            String note = latest != null && !latest.getPaymentRejectionReason().isBlank()
                    ? latest.getPaymentRejectionReason()
                    : latest != null && !latest.getRejectionReason().isBlank()
                    ? latest.getRejectionReason() : vehicle.getRejectionReason();
            model.addRow(new Object[]{vehicle.getVehicleId(), vehicle.getPlateNumber(), vehicle.getVehicleType(),
                    vehicle.getStickerCategory(), service.getVehicleOwnerName(vehicle.getVehicleId()), status,
                    displayDate(vehicle.getApprovalDate()), displayDate(vehicle.getExpirationDate()), paymentStatus, note});
        }
        updateActionStates();
    }

    private String displayDate(LocalDate date) { return date == null ? "-" : date.toString(); }

    private boolean matchesFilter(Vehicle vehicle, VehicleApplication application, String filter) {
        if (filter == null || "All".equals(filter)) return true;
        String status = vehicle.getRegistrationStatus();
        if ("Pending".equals(filter)) return "PENDING".equalsIgnoreCase(status)
                || (application != null && "PENDING".equalsIgnoreCase(application.getStatus()));
        if ("Approved".equals(filter)) return "APPROVED".equalsIgnoreCase(status);
        if ("Rejected".equals(filter)) return "REJECTED".equalsIgnoreCase(status)
                || (application != null && "REJECTED".equalsIgnoreCase(application.getStatus()));
        if ("Expired".equals(filter)) return "EXPIRED".equalsIgnoreCase(status);
        if ("Payment Verification".equals(filter)) return application != null
                && "PENDING_VERIFICATION".equalsIgnoreCase(application.getPaymentStatus());
        if ("Expiring Soon".equals(filter)) {
            if (!"APPROVED".equalsIgnoreCase(status) || vehicle.getExpirationDate() == null) return false;
            long days = ChronoUnit.DAYS.between(LocalDate.now(), vehicle.getExpirationDate());
            return days >= 0 && days <= 7;
        }
        return true;
    }

    private void updateActionStates() {
        Vehicle vehicle = selectedVehicle();
        VehicleApplication application = vehicle == null ? null : service.getLatestVehicleApplication(vehicle.getVehicleId());
        boolean initialStudentRegistration = application != null
                && "STUDENT_INITIAL_REGISTRATION".equals(application.getApplicationType());
        boolean pendingApplication = application != null && "PENDING".equalsIgnoreCase(application.getStatus());
        boolean pendingPayment = application != null && "PENDING_VERIFICATION".equalsIgnoreCase(application.getPaymentStatus());
        approveButton.setEnabled(pendingApplication && !initialStudentRegistration
                && (application.getFeeAmount() == 0 || "VERIFIED".equalsIgnoreCase(application.getPaymentStatus())));
        rejectButton.setEnabled(pendingApplication && !initialStudentRegistration);
        verifyPaymentButton.setEnabled(pendingPayment);
        rejectPaymentButton.setEnabled(pendingPayment);
        proofButton.setEnabled(application != null && application.getReceiptImage() != null);
        boolean expiringSoon = vehicle != null && vehicle.getExpirationDate() != null
                && "APPROVED".equalsIgnoreCase(vehicle.getRegistrationStatus())
                && ChronoUnit.DAYS.between(LocalDate.now(), vehicle.getExpirationDate()) >= 0
                && ChronoUnit.DAYS.between(LocalDate.now(), vehicle.getExpirationDate()) <= 7;
        remindButton.setEnabled(expiringSoon);
        renewButton.setEnabled(vehicle != null && service.getVehicleRenewalFee(vehicle.getVehicleId()) >= 0
                && !(application != null && "RENEWAL".equals(application.getApplicationType())
                && "PENDING".equalsIgnoreCase(application.getStatus())));
        submitPaymentButton.setEnabled(application != null && "PENDING".equalsIgnoreCase(application.getStatus())
                && application.getFeeAmount() > 0 && !"VERIFIED".equalsIgnoreCase(application.getPaymentStatus()));
    }

    private static final class VehicleCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                 boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (!selected && value instanceof String) {
                String text = ((String) value).toUpperCase();
                if (text.contains("APPROVED") || text.equals("VERIFIED") || text.equals("NOT_REQUIRED")) component.setForeground(Theme.STATUS_GREEN);
                else if (text.contains("REJECTED") || text.equals("EXPIRED")) component.setForeground(Theme.STATUS_RED);
                else if (text.contains("PENDING")) component.setForeground(Theme.STATUS_ORANGE);
                else component.setForeground(Theme.TEXT_DARK);
            }
            return component;
        }
    }
}
