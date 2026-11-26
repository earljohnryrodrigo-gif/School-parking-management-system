package ui;

import model.ParkingRecord;
import model.ParkingSlot;
import model.Reservation;
import model.UserType;
import model.Vehicle;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class DashboardPanel extends JPanel {

    private final ParkingService service;
    private final Runnable onLogout;

    private final JLabel userBannerLabel = new JLabel("User: -");
    private final JLabel statusInfoLabel = new JLabel("Status: Ready");

    private final DefaultTableModel slotModel = new DefaultTableModel(
            new String[]{"Slot Number", "Slot Type", "Zone Type", "Status", "Assigned / Holder"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final DefaultTableModel reservationModel = new DefaultTableModel(
            new String[]{"Reservation ID", "Requester", "Role", "Slot", "Start Time", "End Time", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final DefaultTableModel vehicleModel = new DefaultTableModel(
            new String[]{"Vehicle ID", "Plate Number", "Vehicle Type", "Sticker Category", "Owner", "Expiration Date"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final DefaultTableModel recordModel = new DefaultTableModel(
            new String[]{"Record ID", "Vehicle ID", "Slot", "Visitor ID", "Entry Time", "Exit Time", "ID Surrendered"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTextField slotInput = new JTextField(8);
    private final JLabel startLabel = new JLabel("Start:");
    private final JTextField startInput = new JTextField(defaultReservationStart(), 13);
    private final JLabel endLabel = new JLabel("End:");
    private final JTextField endInput = new JTextField(defaultReservationEnd(), 13);

    private final JButton selectImmediateBtn = new JButton("Select Slot (Immediate)");
    private final JButton requestReservationBtn = new JButton("Request Reservation");
    private final JButton releaseBtn = new JButton("Release / Exit Slot");
    private final JButton addSlotBtn = new JButton("+ Add Slot");

    private final JTabbedPane tabs = new JTabbedPane();
    private JPanel slotsTabPanel;
    private JPanel reservationsTabPanel;
    private JPanel vehiclesTabPanel;
    private JPanel recordsTabPanel;

    private final JPanel adminResActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel adminVehicleActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));

    public DashboardPanel(ParkingService service, Runnable onLogout) {
        this.service = service;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private static String defaultReservationStart() {
        return LocalDate.now().plusDays(1) + " 08:00";
    }

    private static String defaultReservationEnd() {
        return LocalDate.now().plusDays(1) + " 17:00";
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(62, 62, 62));
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel brand = new JLabel("Parking-ka-BOSS  |  School Parking Management");
        brand.setForeground(new Color(245, 166, 35));
        brand.setFont(new Font("SansSerif", Font.BOLD, 16));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        userBannerLabel.setForeground(Color.WHITE);
        userBannerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            service.logout();
            onLogout.run();
        });

        right.add(userBannerLabel);
        right.add(logoutBtn);

        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        slotsTabPanel = buildSlotsTab();
        reservationsTabPanel = buildReservationsTab();
        vehiclesTabPanel = buildVehiclesTab();
        recordsTabPanel = buildRecordsTab();

        tabs.addTab("Parking Slots", slotsTabPanel);
        tabs.addTab("Reservations", reservationsTabPanel);
        tabs.addTab("Registered Vehicles", vehiclesTabPanel);
        tabs.addTab("Entry / Exit Logs", recordsTabPanel);
        return tabs;
    }

    private void rebuildTabsForRole(UserType role) {
        Component selected = tabs.getSelectedComponent();
        tabs.removeAll();

        tabs.addTab("Parking Slots", slotsTabPanel);
        if (role != UserType.STUDENT) {
            tabs.addTab("Reservations", reservationsTabPanel);
        }
        if (role == UserType.PARKING_ADMINISTRATOR) {
            tabs.addTab("Registered Vehicles", vehiclesTabPanel);
            tabs.addTab("Entry / Exit Logs", recordsTabPanel);
        }

        if (selected != null && tabs.indexOfComponent(selected) >= 0) {
            tabs.setSelectedComponent(selected);
        } else {
            tabs.setSelectedIndex(0);
        }
    }

    private JPanel buildSlotsTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.add(new JLabel("Slot ID:"));
        controls.add(slotInput);
        controls.add(startLabel);
        controls.add(startInput);
        controls.add(endLabel);
        controls.add(endInput);
        controls.add(selectImmediateBtn);
        controls.add(requestReservationBtn);
        controls.add(releaseBtn);
        controls.add(addSlotBtn);

        JTable table = new JTable(slotModel);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                slotInput.setText(slotModel.getValueAt(row, 0).toString());
            }
        });

        selectImmediateBtn.addActionListener(e -> handleSelectSlotImmediate());
        requestReservationBtn.addActionListener(e -> handleRequestReservation());
        releaseBtn.addActionListener(e -> handleReleaseSlot());
        addSlotBtn.addActionListener(e -> handleAddSlot());

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        JTable table = new JTable(reservationModel);
        table.setRowHeight(24);

        JButton approveBtn = new JButton("Approve Selected");
        JButton rejectBtn = new JButton("Reject Selected");

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a reservation row first.");
                return;
            }
            String resId = reservationModel.getValueAt(row, 0).toString();
            String result = service.approveReservation(resId);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Reservation " + resId + " approved.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Action Failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a reservation row first.");
                return;
            }
            String resId = reservationModel.getValueAt(row, 0).toString();
            String result = service.rejectReservation(resId);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Reservation " + resId + " rejected.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Action Failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        adminResActions.add(approveBtn);
        adminResActions.add(rejectBtn);

        panel.add(adminResActions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVehiclesTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        JTable table = new JTable(vehicleModel);
        table.setRowHeight(24);

        JButton registerBtn = new JButton("+ Register Vehicle");
        registerBtn.addActionListener(e -> handleRegisterVehicle());
        adminVehicleActions.add(registerBtn);

        panel.add(adminVehicleActions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JTable table = new JTable(recordModel);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        statusInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(statusInfoLabel, BorderLayout.WEST);
        return footer;
    }

    private void handleSelectSlotImmediate() {
        ParkingService.UserAccount user = service.getCurrentUser();
        if (user == null) return;
        String slotNumber = slotInput.getText().trim();
        if (slotNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a slot row or enter a Slot ID.");
            return;
        }

        UserType role = user.getUserType();
        if (role == UserType.STUDENT) {
            String result = service.selectSlotForStudent(slotNumber);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Slot " + slotNumber.toUpperCase() + " selected for immediate use.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Selection Blocked", JOptionPane.WARNING_MESSAGE);
            }
        } else if (role == UserType.SCHOOL_PERSONNEL) {
            String result = service.selectSlotForPersonnel(slotNumber);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Slot " + slotNumber.toUpperCase() + " selected for immediate use.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Selection Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleRequestReservation() {
        ParkingService.UserAccount user = service.getCurrentUser();
        if (user == null) return;
        String slotNumber = slotInput.getText().trim();
        if (slotNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a slot row or enter a Slot ID.");
            return;
        }

        UserType role = user.getUserType();
        if (role == UserType.VISITOR || role == UserType.SCHOOL_PERSONNEL) {
            String result = service.requestReservation(slotNumber, startInput.getText().trim(), endInput.getText().trim());
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Reservation request submitted for administrator approval.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Request Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleReleaseSlot() {
        String slotNumber = slotInput.getText().trim();
        if (slotNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a slot row or enter a Slot ID.");
            return;
        }
        String result = service.releaseSlot(slotNumber);
        if ("OK".equals(result)) {
            refreshAll();
            JOptionPane.showMessageDialog(this, "Slot " + slotNumber.toUpperCase() + " is now Available.");
        } else {
            JOptionPane.showMessageDialog(this, result, "Release Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleAddSlot() {
        JTextField numField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Standard", "Motorcycle", "PWD"});
        JComboBox<String> zoneBox = new JComboBox<>(new String[]{"Student", "School Personnel", "Visitor"});

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Slot Number (e.g., S-05):"));
        form.add(numField);
        form.add(new JLabel("Slot Type:"));
        form.add(typeBox);
        form.add(new JLabel("Zone Type:"));
        form.add(zoneBox);

        int choice = JOptionPane.showConfirmDialog(this, form, "Add Parking Slot", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
            String result = service.addSlot(
                    numField.getText(),
                    (String) typeBox.getSelectedItem(),
                    (String) zoneBox.getSelectedItem()
            );
            if ("OK".equals(result)) {
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleRegisterVehicle() {
        JTextField plateField = new JTextField();
        JComboBox<String> vTypeBox = new JComboBox<>(new String[]{"Sedan", "SUV", "Motorcycle", "Van"});
        JComboBox<String> stickerBox = new JComboBox<>(new String[]{"Student", "School Personnel", "Visitor"});
        JTextField ownerField = new JTextField();

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Plate Number:"));
        form.add(plateField);
        form.add(new JLabel("Vehicle Type:"));
        form.add(vTypeBox);
        form.add(new JLabel("Sticker Category:"));
        form.add(stickerBox);
        form.add(new JLabel("Owner Name / ID:"));
        form.add(ownerField);

        int choice = JOptionPane.showConfirmDialog(this, form, "Register Vehicle Sticker", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
            String result = service.registerVehicle(
                    plateField.getText(),
                    (String) vTypeBox.getSelectedItem(),
                    (String) stickerBox.getSelectedItem(),
                    ownerField.getText(),
                    LocalDate.now().plusYears(1)
            );
            if ("OK".equals(result)) {
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void refreshAll() {
        ParkingService.UserAccount user = service.getCurrentUser();
        UserType role = user != null ? user.getUserType() : null;
        boolean isAdmin = role == UserType.PARKING_ADMINISTRATOR;
        boolean isStudent = role == UserType.STUDENT;
        boolean isPersonnel = role == UserType.SCHOOL_PERSONNEL;
        boolean isVisitor = role == UserType.VISITOR;

        if (user != null) {
            userBannerLabel.setText(user.getUserType().getLabel() + ": " + user.getName() + " (" + user.getUserId() + ")");

            selectImmediateBtn.setVisible(isStudent || isPersonnel);
            requestReservationBtn.setVisible(isPersonnel || isVisitor);

            boolean canReserve = isPersonnel || isVisitor;
            startLabel.setVisible(canReserve);
            startInput.setVisible(canReserve);
            endLabel.setVisible(canReserve);
            endInput.setVisible(canReserve);

            if (startInput.getText().trim().isEmpty()) {
                startInput.setText(defaultReservationStart());
            }
            if (endInput.getText().trim().isEmpty()) {
                endInput.setText(defaultReservationEnd());
            }

            releaseBtn.setVisible(!isVisitor);
            addSlotBtn.setVisible(isAdmin);
            adminResActions.setVisible(isAdmin);
            adminVehicleActions.setVisible(isAdmin);

            String assigned = service.getAssignedSlotForCurrentUser();
            statusInfoLabel.setText(assigned != null
                    ? "Active / Assigned Slot: " + assigned
                    : "Active / Assigned Slot: None");
        }

        rebuildTabsForRole(role);

        slotModel.setRowCount(0);
        for (ParkingSlot s : service.getAllSlots()) {
            slotModel.addRow(new Object[]{
                    s.getSlotNumber(),
                    s.getSlotType(),
                    s.getZoneType(),
                    s.checkAvailability() ? "Available" : "Occupied / Reserved",
                    service.getSlotHolderDisplay(s)
            });
        }

        reservationModel.setRowCount(0);
        if (user != null && !isStudent) {
            for (Reservation r : service.getReservations()) {
                if (isAdmin || user.getUserId().equals(r.getRequesterId())) {
                    reservationModel.addRow(new Object[]{
                            r.getReservationId(),
                            r.getRequesterName(),
                            r.getRequesterRole().getLabel(),
                            r.getSlotNumber(),
                            r.getStartDateTime(),
                            r.getEndDateTime(),
                            r.getStatus()
                    });
                }
            }
        }

        vehicleModel.setRowCount(0);
        if (isAdmin) {
            for (Vehicle v : service.getVehicles()) {
                vehicleModel.addRow(new Object[]{
                        v.getVehicleId(),
                        v.getPlateNumber(),
                        v.getVehicleType(),
                        v.getStickerCategory(),
                        v.getOwnerId(),
                        v.getExpirationDate()
                });
            }
        }

        recordModel.setRowCount(0);
        if (isAdmin) {
            for (ParkingRecord rec : service.getRecords()) {
                recordModel.addRow(new Object[]{
                        rec.getRecordId(),
                        rec.getVehicleId(),
                        rec.getSlotNumber(),
                        rec.getVisitorId() != null ? rec.getVisitorId() : "-",
                        rec.getFormattedEntryTime(),
                        rec.getFormattedExitTime(), 
                        rec.isSurrenderedVisitorId() ? "Yes" : "No"
                });
            }
        }
    }
}