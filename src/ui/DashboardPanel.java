package ui;

import model.ParkingRecord;
import model.ParkingSlot;
import model.Reservation;
import model.UserType;
import model.Vehicle;
import model.VehicleApplication;
import service.ParkingService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
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
            new String[]{"Reservation ID", "Requester", "Role", "Plate Number", "Vehicle Type", "Purpose",
                    "Requested Date", "Slot", "Start Time", "End Time", "Status", "Arrived At", "Departed At", "Rejection Reason"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };


    private final DefaultTableModel recordModel = new DefaultTableModel(
            new String[]{"Record ID", "Vehicle ID", "Slot", "Owner / Visitor ID", "Entry Time", "Exit Time", "ID Surrendered"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel userModel = new DefaultTableModel(new String[]{
            "Account ID", "Full Name", "Username", "Account Type", "Student ID / Personnel ID",
            "Course & Year / Department", "Vehicle Type", "Plate Number", "Registration Date",
            "Account Status", "Registration Details / Actions"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final DefaultTableModel pendingStudentModel = new DefaultTableModel(new String[]{
            "Full Name", "Account Type", "ID", "Course / Department", "Username", "Vehicle Type", "Plate Number", "Registration Date", "Registration Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTextField slotInput = new JTextField(8);
    private final JComboBox<VehicleOption> vehicleSelection = new JComboBox<>();
    private final JLabel vehicleSelectionLabel = new JLabel("Vehicle:");
    private final JLabel slotIdLabel = new JLabel("Slot ID:");
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
    private final TabTransitionOverlay transitionOverlay = new TabTransitionOverlay();
    private final NavigationSidebar sidebar;
    private JTable registeredUsersTable;
    private JPanel slotsTabPanel;
    private JPanel reservationsTabPanel;
    private JPanel vehiclesTabPanel;
    private JPanel recordsTabPanel;
    private JPanel usersTabPanel;
    private JPanel campusMapTabPanel;
    private JPanel adminOverviewTabPanel;
    private JPanel zoneOverviewPanel;
    private UserType displayedRole;

    private final JPanel adminResActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    private final JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
    private final JLabel expiryReminderBanner = new JLabel(" ");
    private final JPanel slotManagementNotices = new JPanel();
    private String adminSlotZone = "Student";
    private final JTextField userSearchField = new JTextField(18);
    private final JComboBox<String> userTypeFilter = new JComboBox<>(new String[]{"Students", "School Personnel", "Visitors"});
    private final JLabel pendingStudentHeading = new JLabel("Pending Student and Personnel Registrations");
    private final Timer expiryReminderTimer = new Timer(60_000, e -> refreshExpiryReminderBanner());
    private final Timer visitorNoShowTimer;
    private VehicleManagementPanel vehicleManagementPanel;
    private String registrationPromptedUserId;

    public DashboardPanel(ParkingService service, Runnable onLogout) {
        Theme.installDefaults();
        this.service = service;
        this.onLogout = onLogout;
        this.visitorNoShowTimer = new Timer(1_000, e -> {
            if (this.service.processVisitorNoShows() > 0) refreshAll();
        });
        this.sidebar = new NavigationSidebar(() -> {
            expiryReminderTimer.stop();
            visitorNoShowTimer.stop();
            transitionOverlay.stop();
            registrationPromptedUserId = null;
            service.logout();
            this.onLogout.run();
        });

        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(Theme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        JPanel workspace = new JPanel(new BorderLayout(0, 14));
        workspace.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        workspace.setBackground(Theme.BACKGROUND);
        statsPanel.setOpaque(false);
        expiryReminderBanner.setOpaque(true);
        expiryReminderBanner.setBackground(new Color(185, 28, 28));
        expiryReminderBanner.setForeground(Color.WHITE);
        expiryReminderBanner.setFont(Theme.FONT_LABEL_BOLD);
        expiryReminderBanner.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        expiryReminderBanner.setVisible(false);
        slotManagementNotices.setLayout(new BoxLayout(slotManagementNotices, BoxLayout.Y_AXIS));
        slotManagementNotices.setOpaque(false);
        slotManagementNotices.add(expiryReminderBanner);
        JPanel summaryArea = new JPanel(new BorderLayout(0, 8));
        summaryArea.setOpaque(false);
        summaryArea.add(statsPanel, BorderLayout.NORTH);
        summaryArea.add(slotManagementNotices, BorderLayout.SOUTH);
        workspace.add(summaryArea, BorderLayout.NORTH);
        JPanel contentArea = new JPanel(new BorderLayout(14, 0));
        contentArea.setOpaque(false);
        contentArea.add(sidebar, BorderLayout.WEST);
        buildTabs();
        contentArea.add(buildTabsLayer(), BorderLayout.CENTER);
        workspace.add(contentArea, BorderLayout.CENTER);
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
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        JLabel brand = new JLabel("Parking-ka-BOSS");
        brand.setForeground(Theme.TEXT_WHITE);
        brand.setFont(Theme.FONT_TITLE);
        JLabel subtitle = new JLabel("School Parking Management");
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setFont(Theme.FONT_SUBTITLE);
        heading.add(brand);
        heading.add(Box.createVerticalStrut(2));
        heading.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        userBannerLabel.setForeground(Theme.TEXT_WHITE);
        userBannerLabel.setFont(Theme.FONT_LABEL_BOLD);
        JLabel avatar = new JLabel("O", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        avatar.setForeground(new Color(147, 197, 253));
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setToolTipText("Signed-in profile");
        right.add(avatar);
        right.add(userBannerLabel);

        header.add(heading, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override protected int calculateTabAreaHeight(int placement, int runs, int maxTabHeight) { return 0; }
            @Override protected int calculateTabAreaWidth(int placement, int runs, int maxTabWidth) { return 0; }
            @Override protected void paintTabArea(java.awt.Graphics g, int placement, int selectedIndex) { }
        });
        tabs.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT));
        tabs.setBackground(Theme.CARD_WHITE);
        slotsTabPanel = buildSlotsTab();
        reservationsTabPanel = buildReservationsTab();
        vehiclesTabPanel = buildVehiclesTab();
        recordsTabPanel = buildRecordsTab();
        usersTabPanel = buildUsersTab();
        adminOverviewTabPanel = buildAdminOverviewTab();
        campusMapTabPanel = new CampusParkingMapPanel(service);

        tabs.addTab("Parking Slots", slotsTabPanel);
        tabs.addTab("Reservations", reservationsTabPanel);
        tabs.addTab("Registered Vehicles", vehiclesTabPanel);
        tabs.addTab("Entry / Exit Logs", recordsTabPanel);
        return tabs;
    }

    private JLayeredPane buildTabsLayer() {
        JLayeredPane layer = new JLayeredPane() {
            @Override public void doLayout() {
                tabs.setBounds(0, 0, getWidth(), getHeight());
                transitionOverlay.setBounds(0, 0, getWidth(), getHeight());
            }

            @Override public Dimension getPreferredSize() { return tabs.getPreferredSize(); }
        };
        layer.setOpaque(false);
        layer.add(tabs, JLayeredPane.DEFAULT_LAYER);
        layer.add(transitionOverlay, JLayeredPane.PALETTE_LAYER);
        return layer;
    }

    private JPanel buildAdminOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(Theme.sectionHeader("🏠 Campus Parking Overview", "Live capacity by parking zone"), BorderLayout.NORTH);
        zoneOverviewPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        zoneOverviewPanel.setOpaque(false);
        panel.add(zoneOverviewPanel, BorderLayout.CENTER);
        return panel;
    }

    private void rebuildTabsForRole(UserType role) {
        Component selected = tabs.getSelectedComponent();
        boolean roleChanged = displayedRole != role;
        tabs.removeAll();

        if (role == UserType.VISITOR) {
            tabs.addTab("Reservations", reservationsTabPanel);
            tabs.addTab("Request Reservation", slotsTabPanel);
        } else {
            tabs.addTab("Parking Slots", slotsTabPanel);
        }
        tabs.addTab("Campus Parking Map", campusMapTabPanel);
        if (role == UserType.PARKING_ADMINISTRATOR) {
            tabs.insertTab("Dashboard", null, adminOverviewTabPanel, null, 0);
        }
        if (role == UserType.SCHOOL_PERSONNEL || role == UserType.PARKING_ADMINISTRATOR) {
            tabs.addTab("Reservations", reservationsTabPanel);
        }
        if (role == UserType.STUDENT || role == UserType.SCHOOL_PERSONNEL || role == UserType.PARKING_ADMINISTRATOR) {
            tabs.addTab(role == UserType.PARKING_ADMINISTRATOR ? "Registered Vehicles" : "My Vehicles", vehiclesTabPanel);
            tabs.addTab(role == UserType.PARKING_ADMINISTRATOR ? "Entry / Exit Logs" : "Parking History", recordsTabPanel);
        }
        if (role == UserType.PARKING_ADMINISTRATOR) {
            tabs.addTab("Registered Users", usersTabPanel);
        }

        if (!roleChanged && selected != null && tabs.indexOfComponent(selected) >= 0) {
            tabs.setSelectedComponent(selected);
        } else if (role == UserType.PARKING_ADMINISTRATOR) {
            tabs.setSelectedComponent(adminOverviewTabPanel);
        } else if (role == UserType.VISITOR) {
            tabs.setSelectedComponent(reservationsTabPanel);
        } else if ((role == UserType.STUDENT || role == UserType.SCHOOL_PERSONNEL)
                && !service.canAccessParkingSlotsForCurrentUser()) {
            tabs.setSelectedComponent(vehiclesTabPanel);
        } else {
            tabs.setSelectedComponent(slotsTabPanel);
        }
        displayedRole = role;
        rebuildSidebar(role);
    }

    private void rebuildSidebar(UserType role) {
        java.util.List<NavigationSidebar.NavItem> items = new java.util.ArrayList<>();
        if (role == UserType.VISITOR) {
            addNavItem(items, "📅", "Reservations", reservationsTabPanel);
            addNavItem(items, "📝", "Request Reservation", slotsTabPanel);
            addNavItem(items, "🗺", "Campus Parking Map", campusMapTabPanel);
        } else {
            if (role == UserType.PARKING_ADMINISTRATOR)
                addNavItem(items, "🏠", "Dashboard", adminOverviewTabPanel);
            if (role == UserType.PARKING_ADMINISTRATOR)
                addNavItem(items, "👥", "Registered Users", usersTabPanel);
            if (role == UserType.PARKING_ADMINISTRATOR) {
                addAdminZoneNavItem(items, "🅿", "Student", "Student Parking Slots");
                addAdminZoneNavItem(items, "🅿", "School Personnel", "School Personnel Parking Slots");
                addAdminZoneNavItem(items, "🅿", "Visitor", "Visitor Parking Slots");
            } else {
                addNavItem(items, "🅿", "Parking Slots", slotsTabPanel);
            }
            if (role == UserType.SCHOOL_PERSONNEL || role == UserType.PARKING_ADMINISTRATOR)
                addNavItem(items, "📅", "Reservations", reservationsTabPanel);
            addNavItem(items, "🗺", "Campus Parking Map", campusMapTabPanel);
            if (role == UserType.STUDENT || role == UserType.SCHOOL_PERSONNEL || role == UserType.PARKING_ADMINISTRATOR) {
                addNavItem(items, "🚘", role == UserType.PARKING_ADMINISTRATOR ? "Registered Vehicles" : "My Vehicles", vehiclesTabPanel);
                if (role == UserType.PARKING_ADMINISTRATOR) {
                    items.add(new NavigationSidebar.NavItem("💳", "Payment Verification", () -> {
                        navigateTo(vehiclesTabPanel, "Payment Verification");
                        vehicleManagementPanel.showPaymentVerification();
                    }));
                }
                addNavItem(items, "🕒", role == UserType.PARKING_ADMINISTRATOR ? "Entry / Exit Logs" : "Parking History", recordsTabPanel);
            }
        }
        Component selected = tabs.getSelectedComponent();
        String selectedLabel = navigationLabelFor(selected, role);
        sidebar.setItems(items, selectedLabel);
        if (role == UserType.PARKING_ADMINISTRATOR) {
            sidebar.setBadge("Reservations", service.getPendingVisitorReservationCount());
            sidebar.setBadge("Registered Users", service.getPendingAccountRegistrationCount());
            sidebar.setBadge("Payment Verification", service.getPendingPaymentVerificationCount());
        }
    }

    private void addAdminZoneNavItem(java.util.List<NavigationSidebar.NavItem> items, String icon,
                                     String zone, String label) {
        items.add(new NavigationSidebar.NavItem(icon, label, () -> {
            adminSlotZone = zone;
            slotInput.setText("");
            navigateTo(slotsTabPanel, label);
            refreshAll();
        }));
    }

    private void addNavItem(java.util.List<NavigationSidebar.NavItem> items, String icon, String label, Component target) {
        items.add(new NavigationSidebar.NavItem(icon, label, () -> {
            UserType role = service.getCurrentUser() == null ? null : service.getCurrentUser().getUserType();
            if (target == slotsTabPanel && (role == UserType.STUDENT || role == UserType.SCHOOL_PERSONNEL)
                    && !service.canAccessParkingSlotsForCurrentUser()) {
                navigateTo(vehiclesTabPanel, "My Vehicles");
                JOptionPane.showMessageDialog(this, service.getVehicleAccessMessageForCurrentUser(),
                        "Vehicle Approval Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            navigateTo(target, label);
        }));
    }

    private void navigateTo(Component target, String label) {
        if (target == tabs.getSelectedComponent()) {
            sidebar.setSelected(label);
            return;
        }
        BufferedImage previousView = captureCurrentTab();
        tabs.setSelectedComponent(target);
        tabs.revalidate();
        tabs.repaint();
        sidebar.setSelected(label);
        if (previousView != null) transitionOverlay.fadeFrom(previousView);
    }

    private BufferedImage captureCurrentTab() {
        if (!tabs.isShowing() || tabs.getWidth() < 2 || tabs.getHeight() < 2) return null;
        BufferedImage image = new BufferedImage(tabs.getWidth(), tabs.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        tabs.printAll(graphics);
        graphics.dispose();
        return image;
    }

    private String navigationLabelFor(Component component, UserType role) {
        if (component == adminOverviewTabPanel) return "Dashboard";
        if (component == campusMapTabPanel) return "Campus Parking Map";
        if (role == UserType.PARKING_ADMINISTRATOR && component == slotsTabPanel)
            return adminZoneNavigationLabel(adminSlotZone);
        if (role == UserType.VISITOR && component == slotsTabPanel) return "Request Reservation";
        if (component == reservationsTabPanel) return "Reservations";
        if (component == vehiclesTabPanel) return role == UserType.PARKING_ADMINISTRATOR ? "Registered Vehicles" : "My Vehicles";
        if (component == recordsTabPanel) return role == UserType.PARKING_ADMINISTRATOR ? "Entry / Exit Logs" : "Parking History";
        if (component == usersTabPanel) return "Registered Users";
        return "Parking Slots";
    }

    private String adminZoneNavigationLabel(String zone) {
        if ("School Personnel".equals(zone)) return "School Personnel Parking Slots";
        if ("Visitor".equals(zone)) return "Visitor Parking Slots";
        return "Student Parking Slots";
    }

    private JPanel buildSlotsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controls.setOpaque(false);

        vehicleSelectionLabel.setFont(Theme.FONT_LABEL);
        vehicleSelectionLabel.setForeground(Theme.TEXT_MUTED);
        controls.add(vehicleSelectionLabel);
        controls.add(vehicleSelection);

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
        Theme.styleTable(table);

        selectImmediateBtn.addActionListener(e -> handleSelectSlotImmediate());
        requestReservationBtn.addActionListener(e -> handleRequestReservation());
        releaseBtn.addActionListener(e -> handleReleaseSlot());
        addSlotBtn.addActionListener(e -> handleAddSlot());
        editSlotBtn.addActionListener(e -> handleEditSlot());
        deleteSlotBtn.addActionListener(e -> handleDeleteSlot());

        JPanel slotHeader = new JPanel();
        slotHeader.setOpaque(false);
        slotHeader.setLayout(new BoxLayout(slotHeader, BoxLayout.Y_AXIS));
        slotHeader.add(Theme.sectionHeader("🅿 Parking Slots", "Choose an available campus parking space"));
        slotHeader.add(controls);
        panel.add(slotHeader, BorderLayout.NORTH);
        JScrollPane slotScroll = new JScrollPane(table);
        Theme.styleScrollPane(slotScroll);
        panel.add(slotScroll, BorderLayout.CENTER);
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new StatusTableCellRenderer());
        int[] reservationColumnWidths = {105, 145, 125, 115, 105, 200, 105, 75, 135, 135, 105, 165, 165, 220};
        for (int column = 0; column < reservationColumnWidths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(reservationColumnWidths[column]);
        }
        Theme.styleTable(table);

        JButton approveBtn = Theme.primaryButton("Approve / Assign Selected");
        JButton rejectBtn = Theme.secondaryButton("Reject Selected");
        JButton arrivedBtn = Theme.secondaryButton("Mark Arrived");
        JButton departedBtn = Theme.secondaryButton("Mark Departed");

        Theme.addHoverEffect(approveBtn);
        Theme.addHoverEffect(rejectBtn);
        Theme.addHoverEffect(arrivedBtn);
        Theme.addHoverEffect(departedBtn);

        approveBtn.addActionListener(e -> {
            Reservation selected = selectedReservation(table);
            if (selected == null) return;
            if (selected.getRequesterRole() == UserType.VISITOR) {
                showVisitorAssignmentDialog(selected);
                return;
            }
            showReservationActionResult(service.approveReservation(selected.getReservationId()),
                    "Reservation approved.");
        });

        rejectBtn.addActionListener(e -> {
            Reservation selected = selectedReservation(table);
            if (selected == null) return;
            String reason = JOptionPane.showInputDialog(this, "Enter the reason for rejection:",
                    "Reject Reservation", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) return;
            showReservationActionResult(service.rejectReservation(selected.getReservationId(), reason),
                    "Reservation rejected.");
        });

        arrivedBtn.addActionListener(e -> {
            Reservation selected = selectedReservation(table);
            if (selected == null) return;
            showReservationActionResult(service.markVisitorArrived(selected.getReservationId()),
                    "Visitor arrival recorded.");
        });

        departedBtn.addActionListener(e -> {
            Reservation selected = selectedReservation(table);
            if (selected == null) return;
            showReservationActionResult(service.markVisitorDeparted(selected.getReservationId()),
                    "Visitor departure recorded and the slot released.");
        });

        adminResActions.setOpaque(false);
        adminResActions.add(approveBtn);
        adminResActions.add(rejectBtn);
        adminResActions.add(arrivedBtn);
        adminResActions.add(departedBtn);

        JPanel reservationHeader = new JPanel();
        reservationHeader.setOpaque(false);
        reservationHeader.setLayout(new BoxLayout(reservationHeader, BoxLayout.Y_AXIS));
        reservationHeader.add(Theme.sectionHeader("📅 Reservations", "Review and manage parking requests"));
        reservationHeader.add(adminResActions);
        panel.add(reservationHeader, BorderLayout.NORTH);
        JScrollPane reservationScroll = new JScrollPane(table);
        Theme.styleScrollPane(reservationScroll);
        panel.add(reservationScroll, BorderLayout.CENTER);
        return panel;
    }

    private Reservation selectedReservation(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation row first.");
            return null;
        }
        String reservationId = reservationModel.getValueAt(row, 0).toString();
        for (Reservation reservation : service.getReservations()) {
            if (reservation.getReservationId().equals(reservationId)) return reservation;
        }
        JOptionPane.showMessageDialog(this, "Reservation not found. Refresh the list and try again.",
                "Reservation", JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private void showReservationActionResult(String result, String successMessage) {
        if ("OK".equals(result)) {
            refreshAll();
            JOptionPane.showMessageDialog(this, successMessage);
        } else {
            JOptionPane.showMessageDialog(this, result, "Reservation Action Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showVisitorAssignmentDialog(Reservation reservation) {
        java.util.List<ParkingSlot> availableSlots = service.getAvailableVisitorSlots();
        if (availableSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no available Visitor-zone slots to assign.",
                    "No Visitor Slots Available", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<String> slotBox = new JComboBox<>();
        for (ParkingSlot slot : availableSlots) slotBox.addItem(slot.getSlotNumber());
        JTextField startTime = new JTextField("09:00", 8);
        JTextField endTime = new JTextField("12:00", 8);
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Visitor")); form.add(new JLabel(reservation.getRequesterName()));
        form.add(new JLabel("Plate / Vehicle")); form.add(new JLabel(reservation.getPlateNumber() + " / " + reservation.getVehicleType()));
        form.add(new JLabel("Purpose")); form.add(new JLabel(reservation.getVisitPurpose()));
        form.add(new JLabel("Requested Date")); form.add(new JLabel(String.valueOf(reservation.getRequestedDate())));
        form.add(new JLabel("Available Visitor Slot")); form.add(slotBox);
        form.add(new JLabel("Assigned Time (HH:mm start / end)"));
        JPanel times = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        times.add(startTime); times.add(new JLabel("to")); times.add(endTime); form.add(times);
        int choice = JOptionPane.showConfirmDialog(this, form, "Assign Visitor Reservation",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        String result = service.assignAndApproveVisitorReservation(reservation.getReservationId(),
                String.valueOf(slotBox.getSelectedItem()), startTime.getText(), endTime.getText());
        showReservationActionResult(result, "Visitor slot and time assigned; reservation approved.");
    }

    private JPanel buildVehiclesTab() {
        vehicleManagementPanel = new VehicleManagementPanel(service, this::refreshAll);
        return vehicleManagementPanel;
    }

    private JPanel buildRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(Theme.sectionHeader("🕒 Parking History", "Review completed and active parking records"), BorderLayout.NORTH);
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
        Theme.styleTable(table);
        JScrollPane recordScroll = new JScrollPane(table);
        Theme.styleScrollPane(recordScroll);
        panel.add(recordScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        registeredUsersTable = new JTable(userModel);
        styleRegisteredUsersTable(registeredUsersTable);
        JTable pendingTable = new JTable(pendingStudentModel);
        styleUserTable(pendingTable);
        setColumnWidths(pendingTable, new int[]{150, 125, 125, 170, 125, 115, 130, 115, 135});
        pendingTable.getColumnModel().getColumn(8).setCellRenderer(accountStatusRenderer());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JLabel filterLabel = new JLabel("Account group:");
        filterLabel.setForeground(Theme.TEXT_MUTED);
        userTypeFilter.setFont(Theme.FONT_LABEL);
        Theme.styleInput(userSearchField);
        userSearchField.setToolTipText("Search by name, username, account ID, status, or registration details");
        toolbar.add(filterLabel);
        toolbar.add(userTypeFilter);
        toolbar.add(new JLabel("Search:"));
        toolbar.add(userSearchField);
        JButton refresh = Theme.secondaryButton("Refresh Users");
        refresh.addActionListener(e -> refreshAll());
        toolbar.add(refresh);
        JButton viewDetails = Theme.secondaryButton("View Details");
        viewDetails.addActionListener(e -> showSelectedRegisteredUserDetails());
        toolbar.add(viewDetails);
        panel.add(toolbar, BorderLayout.NORTH);

        pendingStudentHeading.setFont(Theme.FONT_LABEL_BOLD);
        pendingStudentHeading.setForeground(Theme.STATUS_ORANGE);
        pendingStudentHeading.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
        JPanel pendingPanel = new JPanel(new BorderLayout(6, 6));
        pendingPanel.setBackground(Theme.CARD_WHITE);
        pendingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        pendingPanel.add(pendingStudentHeading, BorderLayout.NORTH);
        JScrollPane pendingScroll = new JScrollPane(pendingTable);
        Theme.styleScrollPane(pendingScroll);
        pendingPanel.add(pendingScroll, BorderLayout.CENTER);
        JButton approveStudent = Theme.primaryButton("Approve Account + Initial Vehicle");
        JButton rejectStudent = Theme.secondaryButton("Reject Registration");
        JPanel reviewActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        reviewActions.setOpaque(false);
        reviewActions.add(approveStudent);
        reviewActions.add(rejectStudent);
        pendingPanel.add(reviewActions, BorderLayout.SOUTH);
        approveStudent.addActionListener(e -> reviewSelectedStudentRegistration(pendingTable, true));
        rejectStudent.addActionListener(e -> reviewSelectedStudentRegistration(pendingTable, false));

        JPanel accountsPanel = new JPanel(new BorderLayout(0, 5));
        accountsPanel.setBackground(Theme.CARD_WHITE);
        JLabel accountsHeading = new JLabel("Registered Users");
        accountsHeading.setFont(Theme.FONT_LABEL_BOLD);
        accountsHeading.setForeground(Theme.TEXT_DARK);
        accountsPanel.add(accountsHeading, BorderLayout.NORTH);
        JScrollPane userScroll = new JScrollPane(registeredUsersTable);
        Theme.styleScrollPane(userScroll);
        userScroll.setColumnHeaderView(registeredUsersTable.getTableHeader());
        accountsPanel.add(userScroll, BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pendingPanel, accountsPanel);
        split.setResizeWeight(0.48);
        split.setDividerLocation(245);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);

        registeredUsersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2) showSelectedRegisteredUserDetails();
            }
        });

        userTypeFilter.addActionListener(e -> refreshRegisteredUsers(true));
        userSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshRegisteredUsers(true); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshRegisteredUsers(true); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshRegisteredUsers(true); }
        });
        return panel;
    }

    private void styleUserTable(JTable table) {
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HEADER);
        table.getTableHeader().setReorderingAllowed(false);
        Theme.styleTable(table);
    }

    private void styleRegisteredUsersTable(JTable table) {
        styleUserTable(table);
        table.setRowHeight(36);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnWidths(table, new int[]{100, 160, 110, 125, 155, 175, 135, 160, 120, 115, 190});
        table.getColumnModel().getColumn(9).setCellRenderer(accountStatusRenderer());
        DefaultTableCellRenderer detailsRenderer = new DefaultTableCellRenderer();
        detailsRenderer.setForeground(Theme.PRIMARY_BLUE);
        table.getColumnModel().getColumn(10).setCellRenderer(detailsRenderer);
        table.setToolTipText("Select a user and choose View Details, or double-click a row.");
    }

    private void setColumnWidths(JTable table, int[] widths) {
        for (int index = 0; index < Math.min(widths.length, table.getColumnCount()); index++) {
            table.getColumnModel().getColumn(index).setPreferredWidth(widths[index]);
        }
    }

    private TableCellRenderer accountStatusRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            String status = value == null ? "UNKNOWN" : value.toString().toUpperCase(java.util.Locale.ROOT);
            JLabel badge = Theme.statusBadge(status);
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                badge.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.PRIMARY_BLUE), badge.getBorder()));
            }
            return badge;
        };
    }

    private void reviewSelectedStudentRegistration(JTable table, boolean approve) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a pending Student registration first.");
            return;
        }
        String username = String.valueOf(pendingStudentModel.getValueAt(selectedRow, 4));
        String result;
        if (approve) {
            result = service.approveInitialAccountRegistration(username);
        } else {
            String reason = JOptionPane.showInputDialog(this, "Enter the reason for rejecting this registration:",
                    "Reject Registration", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) return;
            if (reason.isBlank()) {
                JOptionPane.showMessageDialog(this, "A rejection reason is required.", "Reason Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = service.rejectInitialAccountRegistration(username, reason);
        }
        if ("OK".equals(result)) {
            refreshAll();
            JOptionPane.showMessageDialog(this, approve
                    ? "Account and initial vehicle approved together."
                    : "Account and initial vehicle rejected together.");
        } else {
            JOptionPane.showMessageDialog(this, result, "Registration Review", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshRegisteredUsers(boolean isAdmin) {
        if (!isAdmin) return;
        java.util.List<ParkingService.UserAccount> accounts = service.getAccounts();
        java.util.List<Vehicle> vehicles = service.getVehicles();
        java.util.List<Reservation> reservations = service.getReservations();
        String selectedGroup = String.valueOf(userTypeFilter.getSelectedItem());
        String query = userSearchField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        userModel.setRowCount(0);
        for (ParkingService.UserAccount account : accounts) {
            String expectedGroup = account.getUserType() == UserType.STUDENT ? "Students"
                    : account.getUserType() == UserType.SCHOOL_PERSONNEL ? "School Personnel"
                    : account.getUserType() == UserType.VISITOR ? "Visitors" : "";
            if (!expectedGroup.equals(selectedGroup)) continue;
            boolean accountHasPendingReview = account.getUserType() != UserType.VISITOR
                    && "PENDING".equalsIgnoreCase(account.getRegistrationStatus());
            if (accountHasPendingReview) continue;

            String fullName = displayFullName(account);
            String identifier = registeredUserIdentifier(account);
            String program = registeredUserProgram(account);
            String vehicleTypes = registeredUserVehicleTypes(account, vehicles, reservations);
            String plates = registeredUserPlates(account, vehicles, reservations);
            String registeredDate = account.getRegisteredAt() == null ? "—"
                    : account.getRegisteredAt().toLocalDate().toString();
            String details = account.getProfileInfo() == null || account.getProfileInfo().isBlank()
                    ? "No additional registration details" : account.getProfileInfo();
            if (!account.getRegistrationRejectionReason().isBlank())
                details += " | Rejection: " + account.getRegistrationRejectionReason();
            if (account.getUserType() == UserType.VISITOR)
                details += " | Visitor vehicle information is shown from reservation requests.";
            String detailsAction = account.getRegistrationRejectionReason().isBlank()
                    ? "View Details" : "View Details · Rejected";
            String searchable = (account.getUserId() + " " + fullName + " " + account.getName() + " "
                    + account.getUserType().getLabel() + " " + identifier + " " + program + " "
                    + vehicleTypes + " " + plates + " " + registeredDate + " "
                    + account.getRegistrationStatus() + " " + details).toLowerCase(java.util.Locale.ROOT);
            if (!query.isEmpty() && !searchable.contains(query)) continue;
            userModel.addRow(new Object[]{account.getUserId(), fullName, account.getName(),
                    account.getUserType().getLabel(), identifier, program, vehicleTypes, plates,
                    registeredDate, account.getRegistrationStatus(), detailsAction});
        }

        pendingStudentModel.setRowCount(0);
        java.util.List<ParkingService.UserAccount> accountList = accounts;
        for (model.VehicleApplication application : service.getPendingStudentRegistrations()) {
            ParkingService.UserAccount account = null;
            for (ParkingService.UserAccount candidate : accountList) {
                if (candidate.getUserId().equals(application.getOwnerId())) { account = candidate; break; }
            }
            if (account == null) continue;
            pendingStudentModel.addRow(new Object[]{application.getApplicantName(), account.getUserType().getLabel(),
                    application.getIdentificationNumber(), application.getProgramOrDepartment(), account.getName(),
                    application.getVehicleType(), application.getPlateNumber(), application.getSubmittedAt().toLocalDate(), account.getRegistrationStatus()});
        }
        pendingStudentHeading.setText("Pending Student and Personnel Registrations (" + pendingStudentModel.getRowCount() + ")");
    }

    private String displayFullName(ParkingService.UserAccount account) {
        String fullName = account.getFullName() == null ? "" : account.getFullName().trim();
        if (fullName.isEmpty() || fullName.equalsIgnoreCase(account.getName())) return "Not provided";
        return fullName;
    }

    private String registeredUserIdentifier(ParkingService.UserAccount account) {
        if (account.getUserType() == UserType.STUDENT) {
            return account.getStudentId().isBlank() ? account.getUserId() : account.getStudentId();
        }
        if (account.getUserType() == UserType.SCHOOL_PERSONNEL) {
            return account.getPersonnelId().isBlank() ? account.getUserId() : account.getPersonnelId();
        }
        return "—";
    }

    private String registeredUserProgram(ParkingService.UserAccount account) {
        if (account.getUserType() == UserType.STUDENT) return emptyValue(account.getCourseYear());
        if (account.getUserType() == UserType.SCHOOL_PERSONNEL) return emptyValue(account.getDepartment());
        return "—";
    }

    private String registeredUserVehicleTypes(ParkingService.UserAccount account, java.util.List<Vehicle> vehicles,
                                              java.util.List<Reservation> reservations) {
        java.util.LinkedHashSet<String> values = new java.util.LinkedHashSet<>();
        for (Vehicle vehicle : vehicles) {
            if (account.getUserId().equals(vehicle.getOwnerId())) values.add(emptyValue(vehicle.getVehicleType()));
        }
        if (account.getUserType() == UserType.VISITOR) {
            for (Reservation reservation : reservations) {
                if (account.getUserId().equals(reservation.getRequesterId()) && reservation.getVehicleType() != null)
                    values.add(reservation.getVehicleType() + " (reservation)");
            }
        }
        return values.isEmpty() ? "—" : String.join("; ", values);
    }

    private String registeredUserPlates(ParkingService.UserAccount account, java.util.List<Vehicle> vehicles,
                                        java.util.List<Reservation> reservations) {
        java.util.LinkedHashSet<String> values = new java.util.LinkedHashSet<>();
        for (Vehicle vehicle : vehicles) {
            if (account.getUserId().equals(vehicle.getOwnerId())) values.add(emptyValue(vehicle.getPlateNumber()));
        }
        if (account.getUserType() == UserType.VISITOR) {
            for (Reservation reservation : reservations) {
                if (account.getUserId().equals(reservation.getRequesterId()) && reservation.getPlateNumber() != null)
                    values.add(reservation.getPlateNumber() + " (reservation)");
            }
        }
        return values.isEmpty() ? "—" : String.join("; ", values);
    }

    private String emptyValue(String value) { return value == null || value.isBlank() ? "Not provided" : value; }

    private void showSelectedRegisteredUserDetails() {
        int selectedRow = registeredUsersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a registered user first.", "View User Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = registeredUsersTable.convertRowIndexToModel(selectedRow);
        String username = String.valueOf(userModel.getValueAt(modelRow, 2));
        ParkingService.UserAccount account = null;
        for (ParkingService.UserAccount candidate : service.getAccounts()) {
            if (candidate.getName().equalsIgnoreCase(username)) { account = candidate; break; }
        }
        if (account == null) {
            JOptionPane.showMessageDialog(this, "That account could not be found. Refresh the user list and try again.",
                    "View User Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextArea text = new JTextArea(formatRegisteredUserDetails(account), 22, 68);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(Theme.TEXT_DARK);
        text.setBackground(Theme.CARD_WHITE);
        text.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(text);
        Theme.styleScrollPane(scroll);
        JOptionPane.showMessageDialog(this, scroll, "Registered User Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatRegisteredUserDetails(ParkingService.UserAccount account) {
        StringBuilder details = new StringBuilder();
        details.append("ACCOUNT INFORMATION\n")
                .append("Full Name: ").append(displayFullName(account)).append('\n')
                .append("Account ID: ").append(account.getUserId()).append('\n')
                .append("Username: ").append(account.getName()).append('\n')
                .append("Account Type: ").append(account.getUserType().getLabel()).append('\n')
                .append("Student ID / Personnel ID: ").append(registeredUserIdentifier(account)).append('\n')
                .append("Course & Year / Department: ").append(registeredUserProgram(account)).append('\n')
                .append("Registration Date: ").append(account.getRegisteredAt() == null ? "—" : account.getRegisteredAt().toLocalDate()).append('\n')
                .append("Account Status: ").append(account.getRegistrationStatus()).append('\n')
                .append("Registration Details: ").append(emptyValue(account.getProfileInfo())).append('\n');
        if (!account.getRegistrationRejectionReason().isBlank())
            details.append("Administrator Rejection Reason: ").append(account.getRegistrationRejectionReason()).append('\n');

        java.util.List<Vehicle> userVehicles = new java.util.ArrayList<>();
        for (Vehicle vehicle : service.getVehicles()) {
            if (account.getUserId().equals(vehicle.getOwnerId())) userVehicles.add(vehicle);
        }
        java.util.List<VehicleApplication> userApplications = service.getVehicleApplications();
        details.append("\nVEHICLES\n");
        if (userVehicles.isEmpty()) details.append("No registered vehicle record is available.\n");
        for (Vehicle vehicle : userVehicles) {
            details.append("• ").append(vehicle.getVehicleType()).append(" — ").append(vehicle.getPlateNumber())
                    .append(" | Status: ").append(vehicle.getRegistrationStatus())
                    .append(" | Approval: ").append(vehicle.getApprovalDate() == null ? "—" : vehicle.getApprovalDate())
                    .append(" | Expires: ").append(vehicle.getExpirationDate() == null ? "—" : vehicle.getExpirationDate())
                    .append(" | Payment: ").append(vehicle.getPaymentStatus()).append('\n');
            for (VehicleApplication application : userApplications) {
                if (!vehicle.getVehicleId().equals(application.getVehicleId())) continue;
                details.append("   Application: ").append(application.getApplicationType())
                        .append(" / ").append(application.getStatus())
                        .append(" | Fee: ₱").append(application.getFeeAmount())
                        .append(" | Payment: ").append(application.getPaymentStatus());
                if (!application.getPaymentMethod().isBlank()) details.append(" via ").append(application.getPaymentMethod());
                if (!application.getPaymentReference().isBlank()) details.append(" | Reference: ").append(application.getPaymentReference());
                if (!application.getRejectionReason().isBlank()) details.append(" | Note: ").append(application.getRejectionReason());
                if (!application.getPaymentRejectionReason().isBlank()) details.append(" | Payment Note: ").append(application.getPaymentRejectionReason());
                details.append('\n');
            }
        }

        if (account.getUserType() == UserType.VISITOR) {
            details.append("\nVISITOR RESERVATIONS\n");
            boolean found = false;
            for (Reservation reservation : service.getReservations()) {
                if (!account.getUserId().equals(reservation.getRequesterId())) continue;
                found = true;
                details.append("• ").append(reservation.getReservationId()).append(" | ")
                        .append(reservation.getRequestedDate()).append(" | ").append(reservation.getVehicleType())
                        .append(" — ").append(reservation.getPlateNumber()).append(" | Status: ").append(reservation.getStatus())
                        .append(" | Slot: ").append(reservation.getSlotNumber() == null ? "Not assigned" : reservation.getSlotNumber())
                        .append(" | Time: ").append(reservation.getStartDateTime() == null ? "Not assigned" : reservation.getStartDateTime())
                        .append(" — ").append(reservation.getEndDateTime() == null ? "Not assigned" : reservation.getEndDateTime());
                if (!reservation.getRejectionReason().isBlank()) details.append(" | Reason: ").append(reservation.getRejectionReason());
                details.append('\n');
            }
            if (!found) details.append("No Visitor reservation requests are available.\n");
        }

        return details.toString();
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
        VehicleOption selectedVehicle = (VehicleOption) vehicleSelection.getSelectedItem();
        String vehicleId = selectedVehicle == null ? null : selectedVehicle.vehicle.getVehicleId();
        if (role == UserType.STUDENT) {
            String result = service.selectSlotForStudent(slotNumber, vehicleId);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Slot " + slotNumber.toUpperCase() + " selected for immediate use.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Selection Blocked", JOptionPane.WARNING_MESSAGE);
            }
        } else if (role == UserType.SCHOOL_PERSONNEL) {
            String result = service.selectSlotForPersonnel(slotNumber, vehicleId);
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
        if (user.getUserType() == UserType.VISITOR) {
            showVisitorRequestForm();
            return;
        }
        String slotNumber = slotInput.getText().trim();
        if (slotNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a slot row or enter a Slot ID.");
            return;
        }

        UserType role = user.getUserType();
        if (role == UserType.SCHOOL_PERSONNEL) {
            VehicleOption selectedVehicle = (VehicleOption) vehicleSelection.getSelectedItem();
            String vehicleId = selectedVehicle == null ? null : selectedVehicle.vehicle.getVehicleId();
            String result = service.requestReservation(slotNumber, startInput.getText().trim(),
                    endInput.getText().trim(), vehicleId);
            if ("OK".equals(result)) {
                refreshAll();
                JOptionPane.showMessageDialog(this, "Reservation request submitted for administrator approval.");
            } else {
                JOptionPane.showMessageDialog(this, result, "Request Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showVisitorRequestForm() {
        ParkingService.UserAccount user = service.getCurrentUser();
        JTextField fullName = new JTextField(user == null ? "" : user.getName(), 22);
        JTextField plateNumber = new JTextField(22);
        JComboBox<String> vehicleType = new JComboBox<>(new String[]{"Car", "Sedan", "SUV", "Van", "Motorcycle"});
        JTextField purpose = new JTextField(22);
        JTextField reservationDate = new JTextField(LocalDate.now().plusDays(1).toString(), 22);
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Full Name")); form.add(fullName);
        form.add(new JLabel("Plate Number")); form.add(plateNumber);
        form.add(new JLabel("Vehicle Type")); form.add(vehicleType);
        form.add(new JLabel("Purpose of Visit")); form.add(purpose);
        form.add(new JLabel("Reservation Date (yyyy-MM-dd)")); form.add(reservationDate);
        for (Component component : form.getComponents()) {
            if (component instanceof JTextField) Theme.styleInput((JTextField) component);
        }
        int choice = JOptionPane.showConfirmDialog(this, form, "Visitor Reservation Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        String result = service.submitVisitorReservation(fullName.getText(), plateNumber.getText(),
                String.valueOf(vehicleType.getSelectedItem()), purpose.getText(), reservationDate.getText());
        showReservationActionResult(result, "Your request is PENDING Administrator review.");
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
        if (service.getCurrentUser() != null
                && service.getCurrentUser().getUserType() == UserType.PARKING_ADMINISTRATOR) {
            zoneBox.setSelectedItem(adminSlotZone);
        }
        JLabel nameHint = new JLabel();
        Runnable updateNameHint = () -> {
            String zone = String.valueOf(zoneBox.getSelectedItem());
            String prefix = "Student".equals(zone) ? "S" : "School Personnel".equals(zone) ? "P" : "V";
            nameHint.setText("Required format: " + prefix + "-01 (example: " + prefix + "-05)");
        };
        updateNameHint.run();
        zoneBox.addActionListener(e -> updateNameHint.run());

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Slot Number:"));
        form.add(numField);
        form.add(new JLabel("Naming rule:"));
        form.add(nameHint);
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

    public void refreshAll() {
        ParkingService.UserAccount user = service.getCurrentUser();
        UserType role = user != null ? user.getUserType() : null;
        boolean isAdmin = role == UserType.PARKING_ADMINISTRATOR;
        boolean isStudent = role == UserType.STUDENT;
        boolean isPersonnel = role == UserType.SCHOOL_PERSONNEL;
        boolean isVisitor = role == UserType.VISITOR;

        if (user != null) {
            if (!expiryReminderTimer.isRunning()) expiryReminderTimer.start();
            if (!visitorNoShowTimer.isRunning()) visitorNoShowTimer.start();
            userBannerLabel.setText(user.getUserType().getLabel() + ": " + user.getName() + " (" + user.getUserId() + ")");

            selectImmediateBtn.setVisible(isStudent || isPersonnel);
            vehicleSelection.setVisible(isStudent || isPersonnel);
            vehicleSelectionLabel.setVisible(isStudent || isPersonnel);
            requestReservationBtn.setVisible(isPersonnel || isVisitor);
            slotInput.setVisible(!isVisitor);
            slotIdLabel.setVisible(!isVisitor);

            boolean canReserve = isPersonnel;
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

            refreshVehicleSelection();

            releaseBtn.setVisible(isAdmin || isStudent || isPersonnel);
            addSlotBtn.setVisible(isAdmin);
            editSlotBtn.setVisible(isAdmin);
            deleteSlotBtn.setVisible(isAdmin);
            adminResActions.setVisible(isAdmin);

            String assigned = service.getAssignedSlotForCurrentUser();
            statusInfoLabel.setText(assigned != null
                    ? "Active / Assigned Slot: " + assigned
                    : "Active / Assigned Slot: None");
        }

        rebuildTabsForRole(role);
        ((CampusParkingMapPanel) campusMapTabPanel).setRole(role);

        statsPanel.removeAll();
        java.util.List<ParkingSlot> visibleSlots = service.getSlotsForUser(role);
        ParkingService.SlotSummary slotSummary = service.getSlotSummary(role);
        java.util.List<Reservation> visibleReservations = service.getReservations();
        if (isAdmin) {
            sidebar.setBadge("Reservations", service.getPendingVisitorReservationCount());
            sidebar.setBadge("Registered Users", service.getPendingAccountRegistrationCount());
            sidebar.setBadge("Payment Verification", service.getPendingPaymentVerificationCount());
        }
        int available = slotSummary.getAvailable();
        int occupied = slotSummary.getOccupied();
        int reserved = slotSummary.getReserved();
        int pendingReservations = countReservations(visibleReservations, "Pending");
        if (isStudent) {
            statsPanel.setLayout(new GridLayout(1, 3, 12, 0));
            statsPanel.add(Theme.statCard("Total Space", String.valueOf(slotSummary.getTotal()), Theme.PRIMARY_BLUE));
            statsPanel.add(Theme.statCard("Available", String.valueOf(available), Theme.STATUS_GREEN));
            statsPanel.add(Theme.statCard("Occupied", String.valueOf(occupied), Theme.STATUS_RED));
        } else if (isPersonnel) {
            statsPanel.setLayout(new GridLayout(1, 4, 12, 0));
            statsPanel.add(Theme.statCard("Total Spaces", String.valueOf(slotSummary.getTotal()), Theme.PRIMARY_BLUE));
            statsPanel.add(Theme.statCard("Available", String.valueOf(available), Theme.STATUS_GREEN));
            statsPanel.add(Theme.statCard("Occupied", String.valueOf(occupied), Theme.STATUS_RED));
            statsPanel.add(Theme.statCard("My Reservations", String.valueOf(visibleReservations.size()), Theme.PRIMARY_NAVY));
        } else if (isVisitor) {
            statsPanel.setLayout(new GridLayout(1, 3, 12, 0));
            statsPanel.add(Theme.statCard("My Requests", String.valueOf(visibleReservations.size()), Theme.PRIMARY_BLUE));
            statsPanel.add(Theme.statCard("Pending", String.valueOf(pendingReservations), Theme.STATUS_ORANGE));
            statsPanel.add(Theme.statCard("Approved", String.valueOf(countReservations(visibleReservations, "Approved")), Theme.STATUS_GREEN));
        } else {
            statsPanel.setLayout(new GridLayout(1, 5, 12, 0));
            statsPanel.add(Theme.statCard("Total Spaces", String.valueOf(slotSummary.getTotal()), Theme.PRIMARY_BLUE));
            statsPanel.add(Theme.statCard("Available", String.valueOf(available), Theme.STATUS_GREEN));
            statsPanel.add(Theme.statCard("Occupied", String.valueOf(occupied), Theme.STATUS_RED));
            statsPanel.add(Theme.statCard("Reserved", String.valueOf(reserved), Theme.STATUS_ORANGE));
            statsPanel.add(Theme.statCard("Pending Reservations", String.valueOf(pendingReservations), Theme.PRIMARY_NAVY));
        }
        statsPanel.revalidate();
        statsPanel.repaint();
        if (isAdmin) refreshZoneOverview(visibleSlots);

        boolean slotListAccessible = !(isStudent || isPersonnel) || service.canAccessParkingSlotsForCurrentUser();
        slotModel.setRowCount(0);
        java.util.List<ParkingSlot> slotsForTable = new java.util.ArrayList<>(isAdmin
                ? service.getSlotsForAdministratorZone(adminSlotZone)
                : service.getSlotsSortedByStatus(role));
        if (isAdmin) {
            slotsForTable.sort(java.util.Comparator
                    .comparingInt((ParkingSlot slot) -> switch (service.classifySlot(slot)) {
                        case AVAILABLE -> 0;
                        case RESERVED -> 1;
                        case OCCUPIED -> 2;
                    })
                    .thenComparing(ParkingSlot::getSlotNumber, String.CASE_INSENSITIVE_ORDER));
        }
        if (slotListAccessible) for (ParkingSlot s : slotsForTable) {
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
        for (Reservation r : visibleReservations) {
            reservationModel.addRow(new Object[]{
                    r.getReservationId(),
                    r.getRequesterName(),
                    r.getRequesterRole().getLabel(),
                    r.getPlateNumber() != null ? r.getPlateNumber() : (r.getVehicleId() == null ? "-" : r.getVehicleId()),
                    r.getVehicleType() == null ? "-" : r.getVehicleType(),
                    r.getVisitPurpose() == null ? "-" : r.getVisitPurpose(),
                    r.getRequestedDate() == null ? "-" : r.getRequestedDate().toString(),
                    r.getSlotNumber() == null ? "-" : r.getSlotNumber(),
                    r.getStartDateTime() == null ? "-" : r.getStartDateTime(),
                    r.getEndDateTime() == null ? "-" : r.getEndDateTime(),
                    r.getStatus(),
                    r.getArrivedAt() == null ? "-" : r.getArrivedAt().toString(),
                    r.getDepartedAt() == null ? "-" : r.getDepartedAt().toString(),
                    r.getRejectionReason().isBlank() ? "-" : r.getRejectionReason()
            });
        }

        vehicleManagementPanel.refreshForCurrentUser();

        recordModel.setRowCount(0);
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

        refreshRegisteredUsers(isAdmin);

        refreshExpiryReminderBanner();
        refreshSlotManagementNotices(isAdmin);
        if (isStudent && user != null && service.getVehiclesForCurrentUser().isEmpty()
                && !user.getUserId().equals(registrationPromptedUserId)) {
            registrationPromptedUserId = user.getUserId();
            SwingUtilities.invokeLater(() -> vehicleManagementPanel.openRegistrationForm());
        }
    }

    private void refreshVehicleSelection() {
        String previousVehicleId = null;
        VehicleOption selected = (VehicleOption) vehicleSelection.getSelectedItem();
        if (selected != null) previousVehicleId = selected.vehicle.getVehicleId();

        DefaultComboBoxModel<VehicleOption> model = new DefaultComboBoxModel<>();
        for (Vehicle vehicle : service.getApprovedVehiclesForParking()) {
            model.addElement(new VehicleOption(vehicle));
        }
        vehicleSelection.setModel(model);
        if (previousVehicleId != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (previousVehicleId.equals(model.getElementAt(i).vehicle.getVehicleId())) {
                    vehicleSelection.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (model.getSize() > 0) vehicleSelection.setSelectedIndex(0);
    }

    private static final class VehicleOption {
        private final Vehicle vehicle;

        private VehicleOption(Vehicle vehicle) { this.vehicle = vehicle; }

        @Override public String toString() {
            return vehicle.getPlateNumber() + " — " + vehicle.getVehicleType();
        }
    }

    private void refreshExpiryReminderBanner() {
        String message = service.getVehicleReminderMessage();
        boolean visible = message != null && !message.isBlank();
        expiryReminderBanner.setText(visible ? "🔔  EXPIRATION REMINDER  |  " + message : " ");
        expiryReminderBanner.setVisible(visible);
        expiryReminderBanner.getParent().revalidate();
        expiryReminderBanner.getParent().repaint();
    }

    private void refreshSlotManagementNotices(boolean isAdmin) {
        slotManagementNotices.removeAll();
        slotManagementNotices.add(expiryReminderBanner);
        if (isAdmin) {
            java.util.List<String> namingIssues = service.getSlotNamingIssues();
            if (!namingIssues.isEmpty()) {
                JLabel label = new JLabel("  Existing slot naming issues: " + String.join("; ", namingIssues));
                label.setOpaque(true);
                label.setBackground(new Color(255, 237, 213));
                label.setForeground(new Color(124, 45, 18));
                label.setFont(Theme.FONT_LABEL_BOLD);
                label.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                slotManagementNotices.add(label);
            }
        }
        slotManagementNotices.revalidate();
        slotManagementNotices.repaint();
    }

    private int countReservations(java.util.List<Reservation> reservations, String status) {
        int count = 0;
        for (Reservation reservation : reservations) if (status.equalsIgnoreCase(reservation.getStatus())) count++;
        return count;
    }

    /** Paints the previous screen over the new one and fades it out after navigation. */
    private static final class TabTransitionOverlay extends JComponent {
        private static final long serialVersionUID = 1L;
        private BufferedImage previousView;
        private int frame;
        private Timer fadeTimer;

        TabTransitionOverlay() {
            setOpaque(false);
            setVisible(false);
        }

        void fadeFrom(BufferedImage image) {
            if (image == null) return;
            if (fadeTimer != null) fadeTimer.stop();
            previousView = image;
            frame = 0;
            setVisible(true);
            repaint();
            fadeTimer = new Timer(16, e -> {
                frame++;
                if (frame >= 8) stop();
                else repaint();
            });
            fadeTimer.start();
        }

        void stop() {
            if (fadeTimer != null) fadeTimer.stop();
            fadeTimer = null;
            previousView = null;
            setVisible(false);
            repaint();
        }

        @Override public boolean contains(int x, int y) { return false; }

        @Override protected void paintComponent(Graphics graphics) {
            if (previousView == null) return;
            Graphics2D g = (Graphics2D) graphics.create();
            float opacity = Math.max(0f, 1f - frame / 8f);
            g.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g.drawImage(previousView, 0, 0, getWidth(), getHeight(), null);
            g.dispose();
        }
    }

    private void refreshZoneOverview(java.util.List<ParkingSlot> slots) {
        zoneOverviewPanel.removeAll();
        addZoneSummary(slots, "Student", "Student Zone", Theme.PRIMARY_BLUE);
        addZoneSummary(slots, "School Personnel", "Personnel Zone", Theme.STATUS_ORANGE);
        addZoneSummary(slots, "Visitor", "Visitor Zone", Theme.STATUS_GREEN);
        zoneOverviewPanel.revalidate();
        zoneOverviewPanel.repaint();
    }

    private void addZoneSummary(java.util.List<ParkingSlot> slots, String zone, String title, Color accent) {
        int total = 0;
        int available = 0;
        for (ParkingSlot slot : slots) {
            if (!zone.equalsIgnoreCase(slot.getZoneType())) continue;
            total++;
            if (service.classifySlot(slot) == ParkingService.SlotStatus.AVAILABLE) available++;
        }
        zoneOverviewPanel.add(Theme.statCard(title + "  |  Available / Total", available + " / " + total, accent));
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

            // Color slot and reservation lifecycle statuses.
            if (value instanceof String && column >= 0) {
                String text = value.toString().trim();
                if (text.equalsIgnoreCase("Available") || text.equalsIgnoreCase("Departed")) {
                    c.setForeground(Theme.STATUS_GREEN);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Occupied") || text.equalsIgnoreCase("Arrived")
                        || text.equalsIgnoreCase("Rejected") || text.equalsIgnoreCase("No-Show")) {
                    c.setForeground(Theme.STATUS_RED);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Reserved") || text.equalsIgnoreCase("Pending")) {
                    c.setForeground(Theme.STATUS_ORANGE);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Approved")) {
                    c.setForeground(Theme.STATUS_GREEN);
                    c.setFont(Theme.FONT_LABEL_BOLD);
                } else if (text.equalsIgnoreCase("Completed") || text.equalsIgnoreCase("Cancelled")) {
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
