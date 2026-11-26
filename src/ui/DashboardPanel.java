package ui;

import model.ParkingRecord;
import model.ParkingSlot;
import model.Reservation;
import model.UserType;
import model.Vehicle;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
            new String[]{"Record ID", "Vehicle ID", "Slot", "Owner / Visitor ID", "Entry Time", "Exit Time", "ID Surrendered"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel userModel = new DefaultTableModel(new String[]{"Account ID", "Name", "Account Type", "Registration Info"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTextField slotInput = new JTextField(8);
    private final JLabel startLabel = new JLabel("Start:");
    private final JTextField startInput = new JTextField(defaultReservationStart(), 13);
    private final JLabel endLabel = new JLabel("End:");
    private final JTextField endInput = new JTextField(defaultReservationEnd(), 13);

    private final JButton selectImmediateBtn = Theme.primaryButton("Select Slot (Immediate)");
    private final JButton requestReservationBtn = Theme.primaryButton("Request Reservation");
    private final JButton releaseBtn = Theme.primaryButton("Release / Exit Slot");
    private final JButton addSlotBtn = Theme.secondaryButton("+ Add Slot");
    private final JButton editSlotBtn = Theme.secondaryButton("Edit Slot");
    private final JButton deleteSlotBtn = Theme.secondaryButton("Delete Slot");

    private final JTabbedPane tabs = new JTabbedPane();
    private JPanel slotsTabPanel;
    private JPanel reservationsTabPanel;
    private JPanel vehiclesTabPanel;
    private JPanel recordsTabPanel;
    private JPanel usersTabPanel;

    private final JPanel adminResActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    private final JPanel adminVehicleActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    private JTable vehicleTable;
    private final JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));

    public DashboardPanel(ParkingService service, Runnable onLogout) {
        this.service = service;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(Theme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        JPanel workspace = new JPanel(new BorderLayout(0, 14));
        workspace.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        workspace.setBackground(Theme.BACKGROUND);
        statsPanel.setOpaque(false);
        workspace.add(statsPanel, BorderLayout.NORTH);
        workspace.add(buildTabs(), BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
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
        header.setBackground(Theme.PRIMARY_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel brand = new JLabel("Parking-ka-BOSS  |  School Parking Management");
        brand.setForeground(Theme.TEXT_WHITE);
        brand.setFont(Theme.FONT_TITLE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        userBannerLabel.setForeground(Theme.TEXT_WHITE);
        userBannerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton logoutBtn = Theme.secondaryButton("Logout");
        logoutBtn.setMaximumSize(new Dimension(100, 32));
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
        tabs.setTabPlacement(JTabbedPane.LEFT);
        slotsTabPanel = buildSlotsTab();
        reservationsTabPanel = buildReservationsTab();
        vehiclesTabPanel = buildVehiclesTab();
        recordsTabPanel = buildRecordsTab();
        usersTabPanel = buildUsersTab();

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
        if (role != null) {
            tabs.addTab("Reservations", reservationsTabPanel);
        }
        if (role != null) {
            tabs.addTab(role == UserType.PARKING_ADMINISTRATOR ? "Registered Vehicles" : "My Vehicles", vehiclesTabPanel);
        }
        if (role != null) tabs.addTab(role == UserType.PARKING_ADMINISTRATOR ? "Entry / Exit Logs" : "Parking History", recordsTabPanel);
        if (role == UserType.PARKING_ADMINISTRATOR) {
            tabs.addTab("Registered Users", usersTabPanel);
        }

        if (selected != null && tabs.indexOfComponent(selected) >= 0) {
            tabs.setSelectedComponent(selected);
        } else {
            tabs.setSelectedIndex(0);
        }
    }

    private JPanel buildSlotsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setOpaque(false);

        JLabel slotIdLabel = new JLabel("Slot ID:");
        slotIdLabel.setFont(Theme.FONT_LABEL);
        slotIdLabel.setForeground(Theme.TEXT_MUTED);
        controls.add(slotIdLabel);
        controls.add(slotInput);

        controls.add(startLabel);
        controls.add(startInput);
        controls.add(endLabel);
        controls.add(endInput);
        controls.add(selectImmediateBtn);
        controls.add(requestReservationBtn);
        controls.add(releaseBtn);
        controls.add(addSlotBtn);
        controls.add(editSlotBtn);
        controls.add(deleteSlotBtn);

        JTable table = new JTable(slotModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(Theme.PRIMARY_NAVY);
        table.getTableHeader().setForeground(Theme.TEXT_WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(Theme.FONT_TABLE_CELL);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                slotInput.setText(slotModel.getValueAt(row, 0).toString());
            }
        });

        // Custom renderer: color the Status cell and alternate row backgrounds
        table.setDefaultRenderer(Object.class, new StatusTableCellRenderer());

        selectImmediateBtn.addActionListener(e -> handleSelectSlotImmediate());
        requestReservationBtn.addActionListener(e -> handleRequestReservation());
        releaseBtn.addActionListener(e -> handleReleaseSlot());
        addSlotBtn.addActionListener(e -> handleAddSlot());
        editSlotBtn.addActionListener(e -> handleEditSlot());
        deleteSlotBtn.addActionListener(e -> handleDeleteSlot());

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTable table = new JTable(reservationModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(Theme.PRIMARY_NAVY);
        table.getTableHeader().setForeground(Theme.TEXT_WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(Theme.FONT_TABLE_CELL);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setDefaultRenderer(Object.class, new StatusTableCellRenderer());

        JButton approveBtn = Theme.primaryButton("Approve Selected");
        JButton rejectBtn = Theme.secondaryButton("Reject Selected");

        Theme.addHoverEffect(approveBtn);
        Theme.addHoverEffect(rejectBtn);

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

        adminResActions.setOpaque(false);
        adminResActions.add(approveBtn);
        adminResActions.add(rejectBtn);

        panel.add(adminResActions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVehiclesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTable table = new JTable(vehicleModel);
        vehicleTable = table;
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(Theme.PRIMARY_NAVY);
        table.getTableHeader().setForeground(Theme.TEXT_WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(Theme.FONT_TABLE_CELL);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setDefaultRenderer(Object.class, new StatusTableCellRenderer());

        JButton registerBtn = Theme.primaryButton("+ Add Vehicle");
        JButton editBtn = Theme.secondaryButton("Edit Selected");
        JButton deleteBtn = Theme.secondaryButton("Delete Selected");
        JButton refreshBtn = Theme.secondaryButton("Refresh");
        Theme.addHoverEffect(registerBtn);
        Theme.addHoverEffect(editBtn);
        Theme.addHoverEffect(deleteBtn);
        Theme.addHoverEffect(refreshBtn);
        registerBtn.addActionListener(e -> handleRegisterVehicle());
        editBtn.addActionListener(e -> handleEditVehicle());
        deleteBtn.addActionListener(e -> handleDeleteVehicle());
        refreshBtn.addActionListener(e -> refreshAll());

        adminVehicleActions.setOpaque(false);
        adminVehicleActions.add(registerBtn);
        adminVehicleActions.add(editBtn);
        adminVehicleActions.add(deleteBtn);
        adminVehicleActions.add(refreshBtn);

        panel.add(adminVehicleActions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTable table = new JTable(recordModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(Theme.PRIMARY_NAVY);
        table.getTableHeader().setForeground(Theme.TEXT_WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(Theme.FONT_TABLE_CELL);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(235, 245, 255));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setDefaultRenderer(Object.class, new StatusTableCellRenderer());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JTable table = new JTable(userModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setReorderingAllowed(false);
        JButton refresh = Theme.secondaryButton("Refresh Users");
        refresh.addActionListener(e -> refreshAll());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(refresh);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.CARD_WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        statusInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusInfoLabel.setForeground(Theme.TEXT_MUTED);
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

    private void handleEditSlot() {
        String slotNumber = slotInput.getText().trim();
        ParkingSlot selected = service.findSlot(slotNumber);
        if (selected == null) { JOptionPane.showMessageDialog(this, "Select a slot row first."); return; }
        JTextField typeField = new JTextField(selected.getSlotType());
        JComboBox<String> zoneBox = new JComboBox<>(new String[]{"Student", "School Personnel", "Visitor"});
        zoneBox.setSelectedItem(selected.getZoneType());
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Slot type:")); form.add(typeField);
        form.add(new JLabel("Zone:")); form.add(zoneBox);
        if (JOptionPane.showConfirmDialog(this, form, "Edit " + selected.getSlotNumber(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        String result = service.updateSlot(slotNumber, typeField.getText(), (String) zoneBox.getSelectedItem());
        if ("OK".equals(result)) refreshAll(); else JOptionPane.showMessageDialog(this, result, "Update Failed", JOptionPane.WARNING_MESSAGE);
    }

    private void handleDeleteSlot() {
        String slotNumber = slotInput.getText().trim();
        if (slotNumber.isEmpty()) { JOptionPane.showMessageDialog(this, "Select a slot row first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete slot " + slotNumber + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String result = service.deleteSlot(slotNumber);
        if ("OK".equals(result)) { slotInput.setText(""); refreshAll(); }
        else JOptionPane.showMessageDialog(this, result, "Delete Failed", JOptionPane.WARNING_MESSAGE);
    }

    private void handleRegisterVehicle() {
        JTextField plateField = new JTextField();
        JComboBox<String> vTypeBox = new JComboBox<>(new String[]{"Sedan", "SUV", "Motorcycle", "Van"});
        JComboBox<String> stickerBox = new JComboBox<>(new String[]{"Student", "School Personnel", "Visitor"});
        JTextField ownerField = new JTextField();
        boolean admin = service.getCurrentUser() != null && service.getCurrentUser().getUserType() == UserType.PARKING_ADMINISTRATOR;

        JPanel form = new JPanel(new GridLayout(admin ? 4 : 3, 2, 6, 6));
        form.add(new JLabel("Plate Number:"));
        form.add(plateField);
        form.add(new JLabel("Vehicle Type:"));
        form.add(vTypeBox);
        form.add(new JLabel("Sticker Category:"));
        form.add(stickerBox);
        if (admin) {
            form.add(new JLabel("Owner Name / ID:"));
            form.add(ownerField);
        }

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
                tabs.setSelectedComponent(vehiclesTabPanel);
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleEditVehicle() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a vehicle first."); return; }
        String id = vehicleModel.getValueAt(row, 0).toString();
        JTextField plate = new JTextField(vehicleModel.getValueAt(row, 1).toString());
        JComboBox<String> type = new JComboBox<>(new String[]{"Sedan", "SUV", "Motorcycle", "Van"});
        type.setSelectedItem(vehicleModel.getValueAt(row, 2).toString());
        JTextField expiry = new JTextField(vehicleModel.getValueAt(row, 5).toString());
        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Plate number:")); form.add(plate);
        form.add(new JLabel("Vehicle type:")); form.add(type);
        form.add(new JLabel("Expiration (yyyy-MM-dd):")); form.add(expiry);
        if (JOptionPane.showConfirmDialog(this, form, "Edit Vehicle", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String result = service.updateVehicle(id, plate.getText(), (String) type.getSelectedItem(), LocalDate.parse(expiry.getText().trim()));
            if ("OK".equals(result)) refreshAll(); else JOptionPane.showMessageDialog(this, result, "Update Failed", JOptionPane.WARNING_MESSAGE);
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter the date as yyyy-MM-dd.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteVehicle() {
        int row = vehicleTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a vehicle first."); return; }
        String id = vehicleModel.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete the selected vehicle?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String result = service.deleteVehicle(id);
        if ("OK".equals(result)) refreshAll(); else JOptionPane.showMessageDialog(this, result, "Delete Failed", JOptionPane.WARNING_MESSAGE);
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
            editSlotBtn.setVisible(isAdmin);
            deleteSlotBtn.setVisible(isAdmin);
            adminResActions.setVisible(isAdmin);
            adminVehicleActions.setVisible(true);

            String assigned = service.getAssignedSlotForCurrentUser();
            statusInfoLabel.setText(assigned != null
                    ? "Active / Assigned Slot: " + assigned
                    : "Active / Assigned Slot: None");
        }

        rebuildTabsForRole(role);

        statsPanel.removeAll();
        int available = 0;
        int occupied = 0;
        for (ParkingSlot slot : service.getAllSlots()) {
            if (service.classifySlot(slot) == ParkingService.SlotStatus.AVAILABLE) available++;
            else occupied++;
        }
        statsPanel.add(Theme.statCard("Total Spaces", String.valueOf(service.getAllSlots().size()), Theme.PRIMARY_BLUE));
        statsPanel.add(Theme.statCard("Available", String.valueOf(available), Theme.STATUS_GREEN));
        statsPanel.add(Theme.statCard("Occupied / Reserved", String.valueOf(occupied), Theme.STATUS_ORANGE));
        statsPanel.add(Theme.statCard("Reservations", String.valueOf(service.getReservations().size()), Theme.PRIMARY_NAVY));
        statsPanel.revalidate();
        statsPanel.repaint();

        slotModel.setRowCount(0);
        for (ParkingSlot s : service.getSlotsSortedByStatus(role)) {
            String status = switch (service.classifySlot(s)) {
                case AVAILABLE -> "Available";
                case OCCUPIED -> "Occupied";
                case RESERVED -> "Reserved";
            };
            slotModel.addRow(new Object[]{
                    s.getSlotNumber(),
                    s.getSlotType(),
                    s.getZoneType(),
                    status,
                    service.getSlotHolderDisplay(s)
            });
        }

        reservationModel.setRowCount(0);
        if (user != null) {
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
        if (user != null) {
            for (Vehicle v : service.getVehiclesForCurrentUser()) {
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
        if (user != null) {
            for (ParkingRecord rec : service.getRecords()) {
                if (!isAdmin && !user.getUserId().equals(rec.getVisitorId())) continue;
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

        userModel.setRowCount(0);
        if (isAdmin) for (ParkingService.UserAccount account : service.getAccounts()) {
            userModel.addRow(new Object[]{account.getUserId(), account.getName(), account.getUserType().getLabel(), account.getProfileInfo()});
        }
    }

    /**
     * Custom table cell renderer that:
     * - Colors the Status cell green (Available), red (Occupied), or orange (Reserved)
     * - Alternates row background colors (white / very light gray) for readability
     */
    private static class StatusTableCellRenderer implements TableCellRenderer {

        private final TableCellRenderer defaultRenderer;

        StatusTableCellRenderer() {
            this.defaultRenderer = new DefaultTableCellRenderer();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = defaultRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // Alternating row colors
                c.setBackground(row % 2 == 0 ? Theme.CARD_WHITE : new Color(248, 250, 252));
            }

            // Color the Status column (index 3 in slot table, index 6 in reservation table)
            if (value instanceof String && column >= 0) {
                String text = value.toString();
                if (text.equalsIgnoreCase("Available")) {
                    c.setForeground(Theme.STATUS_GREEN);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Occupied") || text.contains("Occupied")) {
                    c.setForeground(Theme.STATUS_RED);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Reserved") || text.contains("Reserved")) {
                    c.setForeground(Theme.STATUS_ORANGE);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Pending") || text.equalsIgnoreCase("Approved")
                        || text.equalsIgnoreCase("Rejected") || text.equalsIgnoreCase("Completed")
                        || text.equalsIgnoreCase("Cancelled")) {
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else {
                    c.setForeground(Theme.TEXT_DARK);
                    c.setFont(Theme.FONT_TABLE_CELL);
                }
            } else {
                c.setForeground(Theme.TEXT_DARK);
                c.setFont(Theme.FONT_TABLE_CELL);
            }

            return c;
        }
    }
}
