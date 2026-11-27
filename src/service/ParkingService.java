package service;

import model.ParkingRecord;
import model.ParkingSlot;
import model.Reservation;
import model.SchoolPersonnel;
import model.Student;
import model.UserType;
import model.Vehicle;
import model.VehicleApplication;
import model.VehicleReminder;
import model.Visitor;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParkingService {

    private static final DateTimeFormatter RESERVATION_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public static class UserAccount {
        private final String userId;
        private final String name;
        private final String password;
        private final UserType userType;
        private final String profileInfo;
        private final String fullName;
        private final String studentId;
        private final String courseYear;
        private String registrationStatus = "APPROVED";
        private String registrationRejectionReason = "";

        public UserAccount(String userId, String name, String password, UserType userType) {
            this(userId, name, password, userType, "");
        }

        public UserAccount(String userId, String name, String password, UserType userType, String profileInfo) {
            this(userId, name, password, userType, profileInfo, name, "", "");
        }

        public UserAccount(String userId, String name, String password, UserType userType, String profileInfo,
                           String fullName, String studentId, String courseYear) {
            this.userId = userId;
            this.name = name;
            this.password = password;
            this.userType = userType;
            this.profileInfo = profileInfo == null ? "" : profileInfo;
            this.fullName = fullName == null ? name : fullName;
            this.studentId = studentId == null ? "" : studentId;
            this.courseYear = courseYear == null ? "" : courseYear;
        }

        public String getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public UserType getUserType() {
            return userType;
        }

        public String getProfileInfo() { return profileInfo; }

        public String getFullName() { return fullName; }

        public String getStudentId() { return studentId; }

        public String getCourseYear() { return courseYear; }

        public String getRegistrationStatus() { return registrationStatus; }

        public void setRegistrationStatus(String status) {
            registrationStatus = status == null ? "APPROVED" : status;
        }

        public String getRegistrationRejectionReason() { return registrationRejectionReason; }

        public void setRegistrationRejectionReason(String reason) {
            registrationRejectionReason = reason == null ? "" : reason;
        }
    }

    private final Map<String, UserAccount> accounts = new HashMap<>();
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, SchoolPersonnel> personnelMap = new HashMap<>();
    private final Map<String, Visitor> visitors = new HashMap<>();

    private final List<ParkingSlot> slots = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<VehicleApplication> vehicleApplications = new ArrayList<>();
    private final List<VehicleReminder> vehicleReminders = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<ParkingRecord> records = new ArrayList<>();

    private UserAccount currentUser;
    private int userCounter = 100;
    private int reservationCounter = 1;
    private int recordCounter = 1;
    // VEH-1 and VEH-2 are reserved for the two seeded demo vehicles.
    private int vehicleCounter = 3;
    private int vehicleApplicationCounter = 1;
    private boolean seedingInitialData;
    private String lastLoginMessage = "";

    public ParkingService() {
        seedingInitialData = true;
        seedInitialData();
        seedingInitialData = false;
    }

    private void seedInitialData() {
        registerAccount("admin", "1234", UserType.PARKING_ADMINISTRATOR);
        registerAccount("student1", "1234", UserType.STUDENT);
        registerAccount("staff1", "1234", UserType.SCHOOL_PERSONNEL);
        registerAccount("visitor1", "1234", UserType.VISITOR);

        slots.add(new ParkingSlot("S-01", "Standard", "Student", true));
        slots.add(new ParkingSlot("S-02", "Standard", "Student", true));
        ParkingSlot s03 = new ParkingSlot("S-03", "Standard", "Student", false);
        Student student1 = students.get("student1");
        if (student1 != null) {
            s03.setAssignedStudentId(student1.getStudentId());
            student1.setAssignedSlotNumber("S-03");
        }
        slots.add(s03);
        slots.add(new ParkingSlot("S-04", "Motorcycle", "Student", true));

        ParkingSlot p01 = new ParkingSlot("P-01", "Standard", "School Personnel", false);
        SchoolPersonnel staff1 = personnelMap.get("staff1");
        if (staff1 != null) {
            p01.setAssignedPersonnelId(staff1.getPersonnelId());
            staff1.setAssignedSlotNumber("P-01");
        }
        slots.add(p01);

        slots.add(new ParkingSlot("P-02", "Standard", "School Personnel", true));
        slots.add(new ParkingSlot("P-03", "Standard", "School Personnel", true));
        slots.add(new ParkingSlot("P-04", "Standard", "School Personnel", true));
        slots.add(new ParkingSlot("P-05", "Standard", "School Personnel", true));

        slots.add(new ParkingSlot("V-01", "Standard", "Visitor", true));
        slots.add(new ParkingSlot("V-02", "Standard", "Visitor", true));

        UserAccount studentAccount = accounts.get("student1");
        UserAccount personnelAccount = accounts.get("staff1");
        vehicles.add(new Vehicle("VEH-1", "ABC-1234", "Sedan", "Student",
                studentAccount != null ? studentAccount.getUserId() : "", LocalDate.now().plusMonths(5)));
        vehicles.add(new Vehicle("VEH-2", "XYZ-9876", "SUV", "School Personnel",
                personnelAccount != null ? personnelAccount.getUserId() : "", LocalDate.now().plusMonths(5)));

        UserAccount visitorAcc = accounts.get("visitor1");
        LocalDate sampleDay = LocalDate.now().plusDays(1);
        Reservation sampleRes = new Reservation(
                "RES-" + (reservationCounter++),
                visitorAcc != null ? visitorAcc.getUserId() : "VIS-104",
                "visitor1",
                UserType.VISITOR,
                null,
                null,
                null,
                "Pending",
                null,
                "DEMO-1234",
                "Sedan",
                "Campus visit",
                sampleDay
        );
        reservations.add(sampleRes);

        ParkingRecord sampleRec = new ParkingRecord("REC-" + (recordCounter++), "VEH-1", "S-03", students.get("student1") != null ? students.get("student1").getStudentId() : null);
        records.add(sampleRec);
    }

    public boolean registerAccount(String name, String password, UserType userType) {
        return registerAccount(name, password, userType, "");
    }

    public boolean registerAccount(String name, String password, UserType userType, String profileInfo) {
        if (isBlank(name) || isBlank(password) || userType == null) {
            return false;
        }
        if (!seedingInitialData) {
            // Public Student registration must include the linked vehicle and await approval.
            // Visitor signup remains the only public use of this generic method.
            boolean publicRole = userType == UserType.VISITOR;
            boolean publicSignup = currentUser == null && publicRole;
            boolean administratorCreation = isCurrentUser(UserType.PARKING_ADMINISTRATOR)
                    && userType != UserType.PARKING_ADMINISTRATOR;
            if (!publicSignup && !administratorCreation) return false;
        }
        String key = name.trim().toLowerCase();
        if (accounts.containsKey(key)) {
            return false;
        }

        String prefix = switch (userType) {
            case STUDENT -> "ST-";
            case SCHOOL_PERSONNEL -> "SP-";
            case VISITOR -> "VIS-";
            case PARKING_ADMINISTRATOR -> "ADM-";
        };
        String id = prefix + (++userCounter);
        String cleanName = name.trim();

        UserAccount acc = new UserAccount(id, cleanName, password, userType, profileInfo == null ? "" : profileInfo.trim());
        accounts.put(key, acc);

        switch (userType) {
            case STUDENT -> students.put(key, new Student(id, cleanName, password));
            case SCHOOL_PERSONNEL -> personnelMap.put(key, new SchoolPersonnel(id, cleanName, password));
            case VISITOR -> visitors.put(key, new Visitor(id, cleanName, password));
            case PARKING_ADMINISTRATOR -> { }
        }
        return true;
    }

    public UserAccount login(String name, String password, UserType expectedType) {
        return authenticate(name, password, expectedType);
    }

    /**
     * Logs in a user and automatically identifies their role from the registered account.
     * No user-type selection is required -- the account already carries the UserType.
     */
    public UserAccount login(String name, String password) {
        return authenticate(name, password, null);
    }

    private UserAccount authenticate(String name, String password, UserType expectedType) {
        lastLoginMessage = "";
        currentUser = null;
        if (isBlank(name) || isBlank(password)) return null;
        UserAccount account = accounts.get(name.trim().toLowerCase(Locale.ROOT));
        if (account == null || !account.getPassword().equals(password)
                || (expectedType != null && account.getUserType() != expectedType)) return null;
        if (account.getUserType() == UserType.STUDENT) {
            if ("PENDING".equalsIgnoreCase(account.getRegistrationStatus())) {
                lastLoginMessage = "Your Student registration is awaiting Parking Administrator approval.";
                return null;
            }
            if ("REJECTED".equalsIgnoreCase(account.getRegistrationStatus())) {
                lastLoginMessage = "Your Student registration was rejected. Administrator reason: "
                        + account.getRegistrationRejectionReason();
                return null;
            }
        }
        currentUser = account;
        return account;
    }

    public String getLastLoginMessage() {
        return lastLoginMessage;
    }

    /** Public combined signup: the Student account and its initial vehicle both wait for review. */
    public String submitStudentRegistration(String fullName, String studentId, String courseYear,
                                            String username, String password, String vehicleType,
                                            String plateNumber) {
        if (currentUser != null) return "Log out before submitting a public Student registration.";
        if (isBlank(fullName) || isBlank(studentId) || isBlank(courseYear) || isBlank(username)
                || isBlank(password) || isBlank(vehicleType) || isBlank(plateNumber)) {
            return "Complete all Student, account, and vehicle fields.";
        }
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (accounts.containsKey(normalizedUsername)) return "That username already exists.";
        String normalizedStudentId = studentId.trim();
        for (Student student : students.values()) {
            if (student.getStudentId().equalsIgnoreCase(normalizedStudentId)) return "That Student ID is already registered.";
        }
        String normalizedPlate = plateNumber.trim().toUpperCase(Locale.ROOT);
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getPlateNumber().equalsIgnoreCase(normalizedPlate)) return "That plate number is already registered.";
        }
        String normalizedVehicleType = vehicleType.trim();
        if (!normalizedVehicleType.equalsIgnoreCase("Motorcycle")
                && !normalizedVehicleType.equalsIgnoreCase("Car")) {
            return "Choose Motorcycle or Car as the vehicle type.";
        }

        String accountId = "ST-" + (++userCounter);
        String cleanFullName = fullName.trim();
        String cleanCourseYear = courseYear.trim();
        String cleanUsername = username.trim();
        UserAccount account = new UserAccount(accountId, cleanUsername, password, UserType.STUDENT,
                "Student ID: " + normalizedStudentId + " | Course & Year: " + cleanCourseYear,
                cleanFullName, normalizedStudentId, cleanCourseYear);
        account.setRegistrationStatus("PENDING");
        accounts.put(normalizedUsername, account);
        students.put(normalizedUsername, new Student(normalizedStudentId, cleanFullName, password));

        String vehicleId = "VEH-" + vehicleCounter++;
        String normalizedType = normalizedVehicleType.equalsIgnoreCase("Motorcycle") ? "Motorcycle" : "Car";
        Vehicle vehicle = new Vehicle(vehicleId, normalizedPlate, normalizedType, "Student", accountId,
                null, "PENDING", null, "", "NOT_REQUIRED");
        vehicles.add(vehicle);
        vehicleApplications.add(new VehicleApplication(nextVehicleApplicationId(), vehicleId, accountId,
                "STUDENT_INITIAL_REGISTRATION", cleanFullName, normalizedStudentId, cleanCourseYear,
                normalizedType, normalizedPlate, 0));
        return "OK";
    }

    /** Admin-only pending initial Student applications for the Registered Users review view. */
    public List<VehicleApplication> getPendingStudentRegistrations() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        List<VehicleApplication> pending = new ArrayList<>();
        for (VehicleApplication application : vehicleApplications) {
            UserAccount account = findAccountById(application.getOwnerId());
            if ("STUDENT_INITIAL_REGISTRATION".equals(application.getApplicationType())
                    && application.getStatus().equalsIgnoreCase("PENDING")
                    && account != null && account.getUserType() == UserType.STUDENT
                    && account.getRegistrationStatus().equalsIgnoreCase("PENDING")) {
                pending.add(application.copy());
            }
        }
        return Collections.unmodifiableList(pending);
    }

    public String approveStudentRegistration(String username) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can approve Student registrations.";
        UserAccount account = findAccount(username);
        if (account == null || account.getUserType() != UserType.STUDENT) return "Student account not found.";
        if (!"PENDING".equalsIgnoreCase(account.getRegistrationStatus())) return "Student registration is already " + account.getRegistrationStatus() + ".";
        VehicleApplication application = findInitialStudentApplication(account.getUserId());
        if (application == null || !"PENDING".equalsIgnoreCase(application.getStatus())) return "Pending initial vehicle application not found.";
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle == null || !"PENDING".equalsIgnoreCase(vehicle.getRegistrationStatus())) return "Pending initial vehicle record not found.";

        LocalDate approvalDate = LocalDate.now();
        account.setRegistrationStatus("APPROVED");
        account.setRegistrationRejectionReason("");
        vehicle.setPlateNumber(application.getPlateNumber());
        vehicle.setVehicleType(application.getVehicleType());
        vehicle.setRegistrationStatus("APPROVED");
        vehicle.setApprovalDate(approvalDate);
        vehicle.setExpirationDate(approvalDate.plusMonths(5));
        vehicle.setRejectionReason("");
        vehicle.setPaymentStatus("NOT_REQUIRED");
        application.setStatus("APPROVED");
        return "OK";
    }

    public String rejectStudentRegistration(String username, String reason) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can reject Student registrations.";
        UserAccount account = findAccount(username);
        if (account == null || account.getUserType() != UserType.STUDENT) return "Student account not found.";
        if (!"PENDING".equalsIgnoreCase(account.getRegistrationStatus())) return "Student registration is already " + account.getRegistrationStatus() + ".";
        if (isBlank(reason)) return "A rejection reason is required.";
        VehicleApplication application = findInitialStudentApplication(account.getUserId());
        if (application == null || !"PENDING".equalsIgnoreCase(application.getStatus())) return "Pending initial vehicle application not found.";
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle == null || !"PENDING".equalsIgnoreCase(vehicle.getRegistrationStatus())) return "Pending initial vehicle record not found.";

        String cleanReason = reason.trim();
        account.setRegistrationStatus("REJECTED");
        account.setRegistrationRejectionReason(cleanReason);
        vehicle.setRegistrationStatus("REJECTED");
        vehicle.setRejectionReason(cleanReason);
        application.setStatus("REJECTED");
        application.setRejectionReason(cleanReason);
        return "OK";
    }

    private UserAccount findAccount(String username) {
        return isBlank(username) ? null : accounts.get(username.trim().toLowerCase(Locale.ROOT));
    }

    private UserAccount findAccountById(String userId) {
        if (isBlank(userId)) return null;
        for (UserAccount account : accounts.values()) if (account.getUserId().equals(userId)) return account;
        return null;
    }

    private VehicleApplication findInitialStudentApplication(String accountId) {
        for (VehicleApplication application : vehicleApplications) {
            if (application.getOwnerId().equals(accountId)
                    && "STUDENT_INITIAL_REGISTRATION".equals(application.getApplicationType())) return application;
        }
        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public UserAccount getCurrentUser() {
        return currentUser;
    }

    public List<UserAccount> getAccounts() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(accounts.values()));
    }

    public List<ParkingSlot> getAllSlots() {
        if (currentUser == null) return Collections.emptyList();
        return getSlotsForUser(currentUser.getUserType());
    }

    /**
     * Returns parking slots filtered by the user's role.
     * - Students: only Student zone slots (S-01 to S-04)
     * - School Personnel: only School Personnel zone slots (P-01 to P-05)
     * - Visitors: only Visitor zone slots (V-01)
     * - Parking Administrators: all parking slots
     */
    public List<ParkingSlot> getSlotsForUser(UserType role) {
        processVisitorNoShows();
        if (role == null || currentUser == null) {
            return Collections.emptyList();
        }
        if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR) {
            if (role == UserType.PARKING_ADMINISTRATOR) return copySlots(slots);
        } else if (role != currentUser.getUserType()) {
            return Collections.emptyList();
        }
        if (currentUser.getUserType() == role
                && (role == UserType.STUDENT || role == UserType.SCHOOL_PERSONNEL)
                && !canAccessParkingSlotsForCurrentUser()) return Collections.emptyList();

        String zone;
        switch (role) {
            case STUDENT -> zone = "Student";
            case SCHOOL_PERSONNEL -> zone = "School Personnel";
            case VISITOR -> zone = "Visitor";
            default -> zone = null;
        }

        List<ParkingSlot> filtered = new ArrayList<>();
        for (ParkingSlot s : slots) {
            if (zone != null && zone.equalsIgnoreCase(s.getZoneType())) {
                filtered.add(s);
            }
        }
        return copySlots(filtered);
    }

    /** Administrator-only view of a single parking zone. */
    public List<ParkingSlot> getSlotsForAdministratorZone(String zoneType) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        String normalizedZone = normalizeZoneType(zoneType);
        if (normalizedZone == null) return Collections.emptyList();
        List<ParkingSlot> filtered = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if (normalizedZone.equalsIgnoreCase(slot.getZoneType())) filtered.add(slot);
        }
        return copySlots(filtered);
    }

    /**
     * Classifies a parking slot's status:
     * - AVAILABLE if the slot is available
     * - RESERVED if the slot is unavailable but has an active reservation
     * - OCCUPIED if the slot is unavailable with no active reservation
     */
    public enum SlotStatus {
        AVAILABLE, OCCUPIED, RESERVED
    }

    public static final class SlotSummary {
        private final int total, available, occupied, reserved;
        private SlotSummary(int total, int available, int occupied, int reserved) {
            this.total = total; this.available = available; this.occupied = occupied; this.reserved = reserved;
        }
        public int getTotal() { return total; }
        public int getAvailable() { return available; }
        public int getOccupied() { return occupied; }
        public int getReserved() { return reserved; }
    }

    /** Aggregate-only capacity data for dashboard cards. */
    public SlotSummary getSlotSummary(UserType role) {
        processVisitorNoShows();
        if (role == null || currentUser == null
                || (currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR && role != currentUser.getUserType())) {
            return new SlotSummary(0, 0, 0, 0);
        }
        int total = 0, available = 0, occupied = 0, reserved = 0;
        for (ParkingSlot slot : slots) {
            String zone = zoneForRole(role);
            if (zone != null && !zone.equalsIgnoreCase(slot.getZoneType())) continue;
            total++;
            SlotStatus status = classifyStoredSlot(slot);
            if (status == SlotStatus.AVAILABLE) available++;
            else if (status == SlotStatus.RESERVED) reserved++;
            else occupied++;
        }
        return new SlotSummary(total, available, occupied, reserved);
    }

    private SlotStatus classifyStoredSlot(ParkingSlot slot) {
        LocalDateTime now = LocalDateTime.now();
        if (hasActiveParkingRecordForSlot(slot.getSlotNumber())) return SlotStatus.OCCUPIED;
        for (Reservation reservation : reservations) {
            if (reservation.getSlotNumber() != null
                    && reservation.getSlotNumber().equalsIgnoreCase(slot.getSlotNumber())
                    && isActiveReservation(reservation, now)) {
                if ("Arrived".equalsIgnoreCase(reservation.getStatus())) return SlotStatus.OCCUPIED;
                if ("Approved".equalsIgnoreCase(reservation.getStatus())) return SlotStatus.RESERVED;
            }
        }
        if (slot.getAssignedStudentId() != null || slot.getAssignedPersonnelId() != null
                || slot.getAssignedVisitorId() != null || slot.getAssignedReservationId() != null
                || !slot.checkAvailability()) return SlotStatus.OCCUPIED;
        return SlotStatus.AVAILABLE;
    }

    public SlotStatus classifySlot(ParkingSlot slot) {
        if (slot == null) {
            return SlotStatus.OCCUPIED;
        }
        ParkingSlot storedSlot = findStoredSlot(slot.getSlotNumber());
        if (!canAccessSlot(storedSlot)) return SlotStatus.OCCUPIED;
        return classifyStoredSlot(storedSlot);
    }

    /** Current capacity warnings by role zone and compatible vehicle class. */
    public List<String> getZoneAvailabilityWarnings() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        processVisitorNoShows();
        List<String> warnings = new ArrayList<>();
        String[][] zones = {{"Student", "Student"}, {"School Personnel", "School Personnel"}, {"Visitor", "Visitor"}};
        String[][] categories = {{"TWO_WHEELER", "2-Wheeler"}, {"FOUR_WHEELER", "4-Wheeler"}};
        for (String[] zone : zones) {
            for (String[] category : categories) {
                boolean available = false;
                for (ParkingSlot slot : slots) {
                    if (zone[0].equalsIgnoreCase(slot.getZoneType())
                            && category[0].equals(slotCategory(slot))
                            && classifyStoredSlot(slot) == SlotStatus.AVAILABLE) {
                        available = true;
                        break;
                    }
                }
                if (!available) warnings.add(zone[1] + " Zone has no available " + category[1] + " parking slots.");
            }
        }
        return Collections.unmodifiableList(warnings);
    }

    /** Names are reported for review only; this method never renames or removes a slot. */
    public List<String> getSlotNamingIssues() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        List<String> issues = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            String issue = slotNameValidationError(slot.getSlotNumber(), slot.getZoneType());
            if (issue != null) issues.add(slot.getSlotNumber() + " in " + slot.getZoneType() + " Zone: " + issue);
        }
        return Collections.unmodifiableList(issues);
    }

    private int statusOrder(SlotStatus status) {
        return switch (status) {
            case AVAILABLE -> 0;
            case OCCUPIED -> 1;
            case RESERVED -> 2;
        };
    }

    /**
     * Returns parking slots filtered by the user's role, sorted by status:
     * Available first, then Occupied, then Reserved. Within each status
     * group, slots are arranged in ascending slot number order.
     */
    public List<ParkingSlot> getSlotsSortedByStatus(UserType role) {
        List<ParkingSlot> filtered = new ArrayList<>(getSlotsForUser(role));
        filtered.sort((a, b) -> {
            int orderA = statusOrder(classifySlot(a));
            int orderB = statusOrder(classifySlot(b));
            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }
            return a.getSlotNumber().compareToIgnoreCase(b.getSlotNumber());
        });
        return filtered;
    }

    public ParkingSlot findSlot(String slotNumber) {
        if (isBlank(slotNumber) || currentUser == null) return null;
        ParkingSlot slot = findStoredSlot(slotNumber);
        return canAccessSlot(slot) ? slot.copy() : null;
    }

    private ParkingSlot findStoredSlot(String slotNumber) {
        if (isBlank(slotNumber)) return null;
        for (ParkingSlot s : slots) {
            if (s.getSlotNumber().equalsIgnoreCase(slotNumber.trim())) {
                return s;
            }
        }
        return null;
    }

    private List<ParkingSlot> copySlots(List<ParkingSlot> source) {
        List<ParkingSlot> copies = new ArrayList<>();
        for (ParkingSlot slot : source) copies.add(slot.copy());
        return Collections.unmodifiableList(copies);
    }

    private boolean canAccessSlot(ParkingSlot slot) {
        if (!canAccessZone(slot)) return false;
        UserType role = currentUser.getUserType();
        return (role != UserType.STUDENT && role != UserType.SCHOOL_PERSONNEL) || canAccessParkingSlotsForCurrentUser();
    }

    private boolean canAccessZone(ParkingSlot slot) {
        if (slot == null || currentUser == null) return false;
        if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR) return true;
        String zone = zoneForRole(currentUser.getUserType());
        return zone != null && zone.equalsIgnoreCase(slot.getZoneType());
    }

    private String zoneForRole(UserType role) {
        return switch (role) {
            case STUDENT -> "Student";
            case SCHOOL_PERSONNEL -> "School Personnel";
            case VISITOR -> "Visitor";
            case PARKING_ADMINISTRATOR -> null;
        };
    }

    private boolean isCurrentUser(UserType role) {
        return currentUser != null && currentUser.getUserType() == role;
    }

    public String getAssignedSlotForCurrentUser() {
        if (currentUser == null) return null;
        String key = currentUser.getName().toLowerCase();
        if (currentUser.getUserType() == UserType.STUDENT) {
            Student st = students.get(key);
            return st != null ? st.viewAssignedSlot() : null;
        }
        if (currentUser.getUserType() == UserType.SCHOOL_PERSONNEL) {
            SchoolPersonnel sp = personnelMap.get(key);
            return sp != null ? sp.viewAssignedSlot() : null;
        }
        return null;
    }

    /** Compatibility overload retained for callers that have not yet supplied a vehicle. */
    public String selectSlotForStudent(String slotNumber) {
        return selectSlotForStudent(slotNumber, null);
    }

    public String selectSlotForStudent(String slotNumber, String vehicleId) {
        if (currentUser == null || currentUser.getUserType() != UserType.STUDENT) {
            return "Only Students can select slots for immediate use.";
        }
        Student st = students.get(currentUser.getName().toLowerCase());
        if (st == null) return "Student profile not found.";
        if (st.viewAssignedSlot() != null) {
            return "You already have an active parking session. Please release your current parking slot before parking another vehicle.";
        }
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (!canAccessZone(slot)) return "Slot not found.";
        if (slot == null) return "Slot not found.";
        if (!"Student".equalsIgnoreCase(slot.getZoneType())) {
            return "Students can only select Student zone slots.";
        }
        Vehicle vehicle = validateSelectedVehicleForParking(vehicleId, slot);
        if (vehicle == null) return selectedVehicleValidationMessage(vehicleId, slot);
        if (classifyStoredSlot(slot) != SlotStatus.AVAILABLE) {
            return "Slot " + slot.getSlotNumber() + " is not available.";
        }

        if (!st.selectSlot(slot)) {
            return "Unable to assign slot " + slot.getSlotNumber() + ".";
        }

        records.add(new ParkingRecord("REC-" + (recordCounter++), vehicle.getVehicleId(), slot.getSlotNumber(), currentUser.getUserId()));
        return "OK";
    }

    /** Compatibility overload retained for callers that have not yet supplied a vehicle. */
    public String selectSlotForPersonnel(String slotNumber) {
        return selectSlotForPersonnel(slotNumber, null);
    }

    public String selectSlotForPersonnel(String slotNumber, String vehicleId) {
        if (currentUser == null || currentUser.getUserType() != UserType.SCHOOL_PERSONNEL) {
            return "Only School Personnel can select slots for immediate use.";
        }
        SchoolPersonnel sp = personnelMap.get(currentUser.getName().toLowerCase());
        if (sp == null) return "Personnel profile not found.";
        if (sp.viewAssignedSlot() != null) {
            return "You already have an active parking session. Please release your current parking slot before parking another vehicle.";
        }
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (!canAccessZone(slot)) return "Slot not found.";
        if (slot == null) return "Slot not found.";
        if (!"School Personnel".equalsIgnoreCase(slot.getZoneType())) {
            return "School Personnel can only select School Personnel zone slots.";
        }
        Vehicle vehicle = validateSelectedVehicleForParking(vehicleId, slot);
        if (vehicle == null) return selectedVehicleValidationMessage(vehicleId, slot);
        if (classifyStoredSlot(slot) != SlotStatus.AVAILABLE) {
            return "Slot " + slot.getSlotNumber() + " is not available.";
        }

        if (!sp.selectSlot(slot)) {
            return "Unable to assign slot " + slot.getSlotNumber() + ".";
        }

        records.add(new ParkingRecord("REC-" + (recordCounter++), vehicle.getVehicleId(), slot.getSlotNumber(), currentUser.getUserId()));
        return "OK";
    }

    /** Returns this account's approved, unexpired vehicles that may be selected for a new session. */
    public List<Vehicle> getApprovedVehiclesForParking() {
        if (!isVehicleOwnerRole()) return Collections.emptyList();
        if (isCurrentUser(UserType.STUDENT)) {
            UserAccount account = findAccount(currentUser.getName());
            if (account == null || !"APPROVED".equalsIgnoreCase(account.getRegistrationStatus())) return Collections.emptyList();
        }
        List<Vehicle> eligible = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (currentUser.getUserId().equals(vehicle.getOwnerId()) && vehicle.isApprovedAndValid()) {
                eligible.add(vehicle.copy());
            }
        }
        return Collections.unmodifiableList(eligible);
    }

    private Vehicle validateSelectedVehicleForParking(String vehicleId, ParkingSlot slot) {
        if (isBlank(vehicleId) || !isVehicleOwnerRole()) return null;
        if (isCurrentUser(UserType.STUDENT)) {
            UserAccount account = findAccount(currentUser.getName());
            if (account == null || !"APPROVED".equalsIgnoreCase(account.getRegistrationStatus())) return null;
        }
        Vehicle vehicle = findStoredVehicle(vehicleId);
        if (vehicle == null || !currentUser.getUserId().equals(vehicle.getOwnerId())) return null;
        if (!vehicle.isApprovedAndValid() || !isVehicleCompatibleWithSlot(vehicle, slot)) return null;
        return vehicle;
    }

    private String selectedVehicleValidationMessage(String vehicleId, ParkingSlot slot) {
        if (isBlank(vehicleId)) return "Select one of your approved vehicles before choosing a parking slot.";
        Vehicle vehicle = findStoredVehicle(vehicleId);
        if (vehicle == null || currentUser == null || !currentUser.getUserId().equals(vehicle.getOwnerId())) {
            return "That vehicle is not registered to the signed-in account.";
        }
        if (vehicle.isExpired() || "EXPIRED".equalsIgnoreCase(vehicle.getRegistrationStatus())) {
            return "The selected vehicle registration has expired. Renew it and wait for approval before parking.";
        }
        if (!"APPROVED".equalsIgnoreCase(vehicle.getRegistrationStatus())) {
            return "The selected vehicle is not approved. Wait for Administrator approval before parking.";
        }
        if (vehicle.getExpirationDate() == null) {
            return "The selected vehicle has no valid approval period. Contact the Administrator before parking.";
        }
        if (!isVehicleCompatibleWithSlot(vehicle, slot)) {
            String vehicleCategory = vehicleCategory(vehicle);
            String requiredCategory = "TWO_WHEELER".equals(vehicleCategory) ? "Motorcycle / 2-Wheeler" : "Car / 4-Wheeler";
            return "This vehicle is not compatible with the selected slot. Choose a " + requiredCategory + " slot.";
        }
        return "An approved, unexpired, compatible vehicle is required.";
    }

    private boolean isVehicleCompatibleWithSlot(Vehicle vehicle, ParkingSlot slot) {
        if (vehicle == null || slot == null) return false;
        return isVehicleTypeCompatibleWithSlot(vehicle.getVehicleType(), slot);
    }

    private boolean isVehicleTypeCompatibleWithSlot(String vehicleType, ParkingSlot slot) {
        if (slot == null) return false;
        String vehicleCategory = vehicleCategory(vehicleType);
        String slotCategory = slotCategory(slot);
        return vehicleCategory != null && vehicleCategory.equals(slotCategory);
    }

    private String vehicleCategory(Vehicle vehicle) {
        return vehicle == null ? null : vehicleCategory(vehicle.getVehicleType());
    }

    private String vehicleCategory(String vehicleType) {
        String type = vehicleType == null ? "" : vehicleType.trim().toLowerCase();
        if (type.equals("motorcycle") || type.equals("motorbike")) return "TWO_WHEELER";
        if (type.equals("car") || type.equals("sedan") || type.equals("suv") || type.equals("van")) return "FOUR_WHEELER";
        return null;
    }

    /** Existing slotType values represent the physical size within each role-specific zone. */
    private String slotCategory(ParkingSlot slot) {
        String type = slot.getSlotType() == null ? "" : slot.getSlotType().trim().toLowerCase();
        if (type.equals("motorcycle") || type.equals("2-wheeler") || type.equals("two-wheeler")) return "TWO_WHEELER";
        if (type.equals("standard") || type.equals("pwd") || type.equals("car")
                || type.equals("4-wheeler") || type.equals("four-wheeler")) return "FOUR_WHEELER";
        return null;
    }

    /** Legacy entry point retained; Personnel must also identify the registered vehicle being reserved. */
    public String requestReservation(String slotNumber, String startDateTime, String endDateTime) {
        return requestReservation(slotNumber, startDateTime, endDateTime, null);
    }

    public String requestReservation(String slotNumber, String startDateTime, String endDateTime, String vehicleId) {
        if (currentUser == null) return "Not logged in.";
        UserType role = currentUser.getUserType();
        if (role == UserType.VISITOR) {
            return "Visitors must submit the Visitor reservation form; the Administrator assigns the slot and time.";
        }
        if (role != UserType.SCHOOL_PERSONNEL) {
            return "Only School Personnel can use this reservation request.";
        }
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (!canAccessZone(slot)) return "Slot not found.";
        if (slot == null) return "Slot not found.";
        if (role == UserType.SCHOOL_PERSONNEL && !"School Personnel".equalsIgnoreCase(slot.getZoneType())) {
            return "School Personnel can only request School Personnel zone slots.";
        }
        Vehicle personnelVehicle = null;
        if (role == UserType.SCHOOL_PERSONNEL) {
            personnelVehicle = validateSelectedVehicleForParking(vehicleId, slot);
            if (personnelVehicle == null) return selectedVehicleValidationMessage(vehicleId, slot);
            SchoolPersonnel holder = personnelMap.get(currentUser.getName().toLowerCase());
            if (holder != null && holder.viewAssignedSlot() != null) {
                return "You already have an assigned slot (" + holder.viewAssignedSlot()
                        + "). Release it before requesting a reservation.";
            }
        }
        if (classifyStoredSlot(slot) != SlotStatus.AVAILABLE) {
            return "Slot " + slot.getSlotNumber() + " is currently not available.";
        }

        String dateError = validateReservationWindow(startDateTime, endDateTime);
        if (dateError != null) {
            return dateError;
        }

        String conflictError = validateReservationConflicts(slot, startDateTime, endDateTime);
        if (conflictError != null) {
            return conflictError;
        }

        Reservation res = new Reservation(
                "RES-" + (reservationCounter++),
                currentUser.getUserId(),
                currentUser.getName(),
                role,
                slot.getSlotNumber(),
                startDateTime.trim(),
                endDateTime.trim(),
                "Pending",
                personnelVehicle == null ? null : personnelVehicle.getVehicleId(),
                null,
                null,
                null,
                null
        );
        reservations.add(res);
        return "OK";
    }

    /** Visitor requests contain no chosen slot or arrival/departure time; the Administrator assigns both later. */
    public String submitVisitorReservation(String fullName, String plateNumber, String vehicleType,
                                           String visitPurpose, String requestedDateText) {
        if (!isCurrentUser(UserType.VISITOR)) return currentUser == null
                ? "Not logged in." : "Only Visitors can submit Visitor reservation requests.";
        if (isBlank(fullName) || isBlank(plateNumber) || isBlank(vehicleType)
                || isBlank(visitPurpose) || isBlank(requestedDateText)) {
            return "Complete your name, plate number, vehicle type, visit purpose, and reservation date.";
        }
        String normalizedType = vehicleType.trim();
        if (!List.of("Car", "Sedan", "SUV", "Van", "Motorcycle").contains(normalizedType)) {
            return "Choose Car, Sedan, SUV, Van, or Motorcycle as the vehicle type.";
        }
        LocalDate requestedDate;
        try {
            requestedDate = LocalDate.parse(requestedDateText.trim());
        } catch (java.time.DateTimeException ex) {
            return "Enter the reservation date as yyyy-MM-dd.";
        }
        if (requestedDate.isBefore(LocalDate.now())) return "The reservation date must be today or a future date.";

        String cleanPlate = plateNumber.trim().toUpperCase();
        for (Reservation existing : reservations) {
            if (currentUser.getUserId().equals(existing.getRequesterId())
                    && existing.getPlateNumber() != null
                    && existing.getPlateNumber().equalsIgnoreCase(cleanPlate)
                    && requestedDate.equals(existing.getRequestedDate())
                    && ("Pending".equalsIgnoreCase(existing.getStatus())
                    || "Approved".equalsIgnoreCase(existing.getStatus())
                    || "Arrived".equalsIgnoreCase(existing.getStatus()))) {
                return "A request for this vehicle and date is already active.";
            }
        }
        reservations.add(new Reservation("RES-" + (reservationCounter++), currentUser.getUserId(),
                fullName.trim(), UserType.VISITOR, null, null, null, "Pending", null,
                cleanPlate, normalizedType, visitPurpose.trim(), requestedDate));
        return "OK";
    }

    /** Administrator-only list of currently available Visitor-zone slots for assignment. */
    public List<ParkingSlot> getAvailableVisitorSlots() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return Collections.emptyList();
        processVisitorNoShows();
        List<ParkingSlot> available = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if ("Visitor".equalsIgnoreCase(slot.getZoneType())
                    && classifyStoredSlot(slot) == SlotStatus.AVAILABLE) {
                available.add(slot.copy());
            }
        }
        return Collections.unmodifiableList(available);
    }

    public int getPendingVisitorReservationCount() {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return 0;
        processVisitorNoShows();
        int count = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getRequesterRole() == UserType.VISITOR
                    && "Pending".equalsIgnoreCase(reservation.getStatus())) count++;
        }
        return count;
    }

    /** Assigns an available Visitor slot and Administrator-selected times before approving the request. */
    public String assignAndApproveVisitorReservation(String reservationId, String slotNumber,
                                                      String startTime, String endTime) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Administrators can assign and approve Visitor reservations.";
        Reservation reservation = findReservation(reservationId);
        if (reservation == null) return "Reservation not found.";
        if (reservation.getRequesterRole() != UserType.VISITOR) return "This action is only for Visitor reservations.";
        if (!"Pending".equalsIgnoreCase(reservation.getStatus())) return "Reservation is already " + reservation.getStatus() + ".";
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (slot == null || !"Visitor".equalsIgnoreCase(slot.getZoneType())) return "Choose a Visitor-zone slot.";
        if (classifyStoredSlot(slot) != SlotStatus.AVAILABLE) return "That slot is occupied or already reserved.";
        if (reservation.getRequestedDate() == null) return "This Visitor request has no valid reservation date.";
        if (!isVehicleTypeCompatibleWithSlot(reservation.getVehicleType(), slot)) {
            return "The selected Visitor slot is not compatible with this vehicle type.";
        }

        String startDateTime = combineReservationDateAndTime(reservation.getRequestedDate(), startTime);
        String endDateTime = combineReservationDateAndTime(reservation.getRequestedDate(), endTime);
        if (startDateTime == null || endDateTime == null) return "Enter start and end times as HH:mm.";
        String windowError = validateReservationWindow(startDateTime, endDateTime);
        if (windowError != null) return windowError;
        String conflict = reservationWindowConflict(slot.getSlotNumber(), startDateTime, endDateTime, reservationId);
        if (conflict != null) return conflict;

        reservation.assignSlotAndTime(slot.getSlotNumber(), startDateTime, endDateTime);
        reservation.approve();
        slot.updateAvailability(false);
        slot.setAssignedVisitorId(reservation.getRequesterId());
        slot.setAssignedReservationId(reservation.getReservationId());
        return "OK";
    }

    private String combineReservationDateAndTime(LocalDate date, String timeText) {
        if (date == null || isBlank(timeText)) return null;
        try {
            LocalTime time = LocalTime.parse(timeText.trim(), DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT));
            return date + " " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (java.time.DateTimeException ex) {
            return null;
        }
    }

    private String reservationWindowConflict(String slotNumber, String startDateTime,
                                            String endDateTime, String exceptReservationId) {
        LocalDateTime start = parseReservationDateTime(startDateTime);
        LocalDateTime end = parseReservationDateTime(endDateTime);
        if (start == null || end == null) return "Invalid reservation date or time.";
        Reservation candidate = findReservation(exceptReservationId);
        for (Reservation existing : reservations) {
            if (existing.getReservationId().equalsIgnoreCase(exceptReservationId)
                    || !isActiveReservation(existing, LocalDateTime.now())) continue;
            LocalDateTime existingStart = parseReservationDateTime(existing.getStartDateTime());
            LocalDateTime existingEnd = parseReservationDateTime(existing.getEndDateTime());
            if (existingStart == null || existingEnd == null || !windowsOverlap(start, end, existingStart, existingEnd)) continue;
            if (existing.getSlotNumber() != null && slotNumber.equalsIgnoreCase(existing.getSlotNumber())) {
                return "That slot already has an overlapping reservation.";
            }
            if (candidate != null && candidate.getRequesterId().equals(existing.getRequesterId())) {
                return "This Visitor already has an overlapping reservation.";
            }
        }
        return null;
    }

    private Reservation findReservation(String reservationId) {
        if (isBlank(reservationId)) return null;
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId().equalsIgnoreCase(reservationId.trim())) return reservation;
        }
        return null;
    }

    public String validateReservationWindow(String startDateTime, String endDateTime) {
        if (isBlank(startDateTime) || isBlank(endDateTime)) {
            return "Start and end date/time are required.";
        }
        LocalDateTime start = parseReservationDateTime(startDateTime);
        LocalDateTime end = parseReservationDateTime(endDateTime);
        if (start == null || end == null) {
            return "Invalid date/time format. Use yyyy-MM-dd HH:mm (e.g., 2026-11-27 08:00).";
        }
        if (!start.isAfter(LocalDateTime.now())) {
            return "Start time must be in the future.";
        }
        if (!end.isAfter(start)) {
            return "End time must be after the start time.";
        }
        return null;
    }

    private String validateReservationConflicts(ParkingSlot slot, String startDateTime, String endDateTime) {
        if (slot == null) return "Choose a valid parking slot.";
        LocalDateTime newStart = parseReservationDateTime(startDateTime);
        LocalDateTime newEnd = parseReservationDateTime(endDateTime);
        if (newStart == null || newEnd == null) {
            return "Invalid date/time format. Use yyyy-MM-dd HH:mm (e.g., 2026-11-27 08:00).";
        }

        String requesterId = currentUser.getUserId();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation existing : reservations) {
            if (!isActiveReservation(existing, now)) continue;
            if (existing.getSlotNumber() == null) continue;

            LocalDateTime exStart = parseReservationDateTime(existing.getStartDateTime());
            LocalDateTime exEnd = parseReservationDateTime(existing.getEndDateTime());
            if (exStart == null || exEnd == null) continue;
            if (!windowsOverlap(newStart, newEnd, exStart, exEnd)) continue;

            boolean sameRequester = requesterId.equals(existing.getRequesterId());
            boolean sameSlot = existing.getSlotNumber().equalsIgnoreCase(slot.getSlotNumber());

            if (sameRequester && sameSlot && exStart.equals(newStart) && exEnd.equals(newEnd)) {
                return "Duplicate reservation: you already requested slot " + slot.getSlotNumber()
                        + " for this time (" + existing.getReservationId() + ").";
            }
            if (sameSlot) {
                return "Slot " + slot.getSlotNumber() + " is already reserved for an overlapping time.";
            }
            if (sameRequester) {
                return "You already have a reservation (" + existing.getReservationId()
                        + ") for slot " + existing.getSlotNumber() + " that overlaps this time.";
            }
        }

        if (currentUser.getUserType() == UserType.SCHOOL_PERSONNEL) {
            for (Reservation existing : reservations) {
                if (isActiveReservation(existing, now) && requesterId.equals(existing.getRequesterId())) {
                    return "You already have an active reservation (" + existing.getReservationId()
                            + ") for slot " + existing.getSlotNumber()
                            + ". School Personnel can hold only one active reservation at a time.";
                }
            }
        }
        return null;
    }

    private String validateApprovalConflicts(Reservation target) {
        LocalDateTime tStart = parseReservationDateTime(target.getStartDateTime());
        LocalDateTime tEnd = parseReservationDateTime(target.getEndDateTime());
        if (tStart == null || tEnd == null) {
            return "Reservation has an invalid date/time. Reject it instead.";
        }
        for (Reservation other : reservations) {
            if (other == target || !"Approved".equalsIgnoreCase(other.getStatus())) continue;
            if (other.getSlotNumber() == null) continue;

            LocalDateTime oStart = parseReservationDateTime(other.getStartDateTime());
            LocalDateTime oEnd = parseReservationDateTime(other.getEndDateTime());
            if (oStart == null || oEnd == null) continue;
            if (!windowsOverlap(tStart, tEnd, oStart, oEnd)) continue;

            if (other.getSlotNumber().equalsIgnoreCase(target.getSlotNumber())) {
                return "Slot " + target.getSlotNumber() + " is already approved for an overlapping time ("
                        + other.getReservationId() + ").";
            }
            if (other.getRequesterId().equals(target.getRequesterId())) {
                return "The requester already has an approved reservation (" + other.getReservationId()
                        + ") that overlaps this time.";
            }
        }
        return null;
    }

    private String validatePersonnelApprovalLimit(Reservation target) {
        if (target.getRequesterRole() != UserType.SCHOOL_PERSONNEL) return null;
        LocalDateTime now = LocalDateTime.now();
        for (Reservation other : reservations) {
            if (other == target) continue;
            if (isActiveReservation(other, now) && target.getRequesterId().equals(other.getRequesterId())) {
                return "You already have an active reservation (" + other.getReservationId()
                        + ") for slot " + other.getSlotNumber()
                        + ". School Personnel can hold only one active reservation at a time.";
            }
        }
        return null;
    }

    private boolean isActiveReservation(Reservation r, LocalDateTime now) {
        if ("Arrived".equalsIgnoreCase(r.getStatus())) return true;
        boolean active = "Pending".equalsIgnoreCase(r.getStatus()) || "Approved".equalsIgnoreCase(r.getStatus());
        if (!active) return false;
        LocalDateTime end = parseReservationDateTime(r.getEndDateTime());
        return end == null || end.isAfter(now);
    }

    private boolean windowsOverlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private LocalDateTime parseReservationDateTime(String text) {
        if (isBlank(text)) return null;
        try {
            return LocalDateTime.parse(text.trim(), RESERVATION_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public List<Reservation> getReservations() {
        processVisitorNoShows();
        if (currentUser == null) return Collections.emptyList();
        List<Reservation> visible = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                    || currentUser.getUserId().equals(reservation.getRequesterId())) {
                visible.add(reservation.copy());
            }
        }
        return Collections.unmodifiableList(visible);
    }

    public String approveReservation(String reservationId) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can approve reservations.";
        }
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(reservationId)) {
                if (!"Pending".equalsIgnoreCase(r.getStatus())) {
                    return "Reservation is already " + r.getStatus() + ".";
                }
                if (r.getRequesterRole() == UserType.VISITOR) {
                    return "Assign an available Visitor slot and time before approving this request.";
                }
                ParkingSlot slot = findStoredSlot(r.getSlotNumber());
                if (slot == null || classifyStoredSlot(slot) != SlotStatus.AVAILABLE) {
                    return "Target slot is no longer available.";
                }
                String expectedZone = r.getRequesterRole() == UserType.VISITOR ? "Visitor" : "School Personnel";
                if (!expectedZone.equalsIgnoreCase(slot.getZoneType())) {
                    return "Slot " + slot.getSlotNumber() + " is not in the requester's zone.";
                }
                String windowError = validateReservationWindow(r.getStartDateTime(), r.getEndDateTime());
                if (windowError != null) {
                    return "Cannot approve: " + windowError + " Reject this reservation instead.";
                }
                String approvalConflict = validateApprovalConflicts(r);
                if (approvalConflict != null) {
                    return approvalConflict;
                }
                String personnelLimit = validatePersonnelApprovalLimit(r);
                if (personnelLimit != null) {
                    return personnelLimit;
                }
                Vehicle reservedVehicle = findStoredVehicle(r.getVehicleId());
                if (reservedVehicle == null || !r.getRequesterId().equals(reservedVehicle.getOwnerId())) {
                    return "The reservation's vehicle is missing or is not owned by the requester.";
                }
                if (!reservedVehicle.isApprovedAndValid()) {
                    return "The reservation's vehicle is pending approval or expired.";
                }
                if (!isVehicleCompatibleWithSlot(reservedVehicle, slot)) {
                    return "The reserved vehicle is not compatible with this slot.";
                }
                r.approve();
                slot.updateAvailability(false);
                slot.setAssignedPersonnelId(r.getRequesterId());
                SchoolPersonnel sp = personnelMap.get(r.getRequesterName().toLowerCase());
                if (sp != null) sp.reservedSlot(slot.getSlotNumber());
                ParkingRecord rec = new ParkingRecord("REC-" + (recordCounter++), r.getVehicleId(), slot.getSlotNumber(), r.getRequesterId());
                records.add(rec);
                return "OK";
            }
        }
        return "Reservation ID not found.";
    }

    public String rejectReservation(String reservationId) {
        return rejectReservation(reservationId, "");
    }

    public String rejectReservation(String reservationId, String reason) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can reject reservations.";
        }
        if (isBlank(reason)) return "A rejection reason is required.";
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(reservationId)) {
                if (!"Pending".equalsIgnoreCase(r.getStatus())) {
                    return "Reservation is already " + r.getStatus() + ".";
                }
                r.reject(reason);
                return "OK";
            }
        }
        return "Reservation ID not found.";
    }

    public String markVisitorArrived(String reservationId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Administrators can mark a Visitor as arrived.";
        Reservation reservation = findReservation(reservationId);
        if (reservation == null) return "Reservation not found.";
        if (reservation.getRequesterRole() != UserType.VISITOR) return "Arrival processing is only for Visitor reservations.";
        if (!"Approved".equalsIgnoreCase(reservation.getStatus())) return "Only an approved Visitor reservation can be marked arrived.";
        ParkingSlot slot = findStoredSlot(reservation.getSlotNumber());
        if (!isSlotHeldByReservation(slot, reservation) || slot.checkAvailability()) {
            return "The assigned slot is no longer reserved by this reservation.";
        }
        if (hasActiveParkingRecordForSlot(slot.getSlotNumber())) {
            return "The assigned slot already has an active parking record.";
        }
        LocalDateTime now = LocalDateTime.now();
        reservation.markArrived(now);
        ParkingRecord record = new ParkingRecord("REC-" + (recordCounter++), reservation.getPlateNumber(),
                slot.getSlotNumber(), reservation.getRequesterId());
        record.markVisitorIdSurrendered(true);
        records.add(record);
        return "OK";
    }

    public String markVisitorDeparted(String reservationId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Administrators can mark a Visitor as departed.";
        Reservation reservation = findReservation(reservationId);
        if (reservation == null) return "Reservation not found.";
        if (reservation.getRequesterRole() != UserType.VISITOR) return "Departure processing is only for Visitor reservations.";
        if (!"Arrived".equalsIgnoreCase(reservation.getStatus())) return "Only an arrived Visitor can be marked departed.";
        ParkingSlot slot = findStoredSlot(reservation.getSlotNumber());
        if (!isSlotHeldByReservation(slot, reservation) || slot.checkAvailability()) {
            return "The assigned slot is no longer occupied by this reservation.";
        }
        ParkingRecord activeRecord = findActiveVisitorRecord(reservation);
        if (activeRecord == null || hasOtherActiveParkingRecordForSlot(slot.getSlotNumber(), activeRecord)) {
            return "The active parking record does not match this reservation; the slot was not released.";
        }

        LocalDateTime now = LocalDateTime.now();
        reservation.markDeparted(now);
        activeRecord.recordExit();
        slot.clearAssignments();
        slot.updateAvailability(true);
        return "OK";
    }

    /** System maintenance operation, called by the dashboard timer and reservation reads. */
    public int processVisitorNoShows() {
        return processVisitorNoShowsAt(LocalDateTime.now());
    }

    private int processVisitorNoShowsAt(LocalDateTime now) {
        int processed = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getRequesterRole() != UserType.VISITOR
                    || !"Approved".equalsIgnoreCase(reservation.getStatus())) continue;
            LocalDateTime start = parseReservationDateTime(reservation.getStartDateTime());
            if (start == null || now.isBefore(start.plusMinutes(30))) continue;

            ParkingSlot slot = findStoredSlot(reservation.getSlotNumber());
            boolean safeToRelease = isSlotHeldByReservation(slot, reservation)
                    && !slot.checkAvailability()
                    && !hasActiveParkingRecordForSlot(slot.getSlotNumber())
                    && !hasOtherOverlappingActiveReservation(reservation, now);
            if (safeToRelease) {
                slot.clearAssignments();
                slot.updateAvailability(true);
            }
            reservation.markNoShow(now);
            processed++;
        }
        return processed;
    }

    private boolean isSlotHeldByReservation(ParkingSlot slot, Reservation reservation) {
        return slot != null && reservation != null
                && reservation.getReservationId().equals(slot.getAssignedReservationId())
                && reservation.getRequesterId().equals(slot.getAssignedVisitorId());
    }

    private boolean hasOtherOverlappingActiveReservation(Reservation reservation, LocalDateTime now) {
        LocalDateTime start = parseReservationDateTime(reservation.getStartDateTime());
        LocalDateTime end = parseReservationDateTime(reservation.getEndDateTime());
        if (start == null || end == null) return true;
        for (Reservation other : reservations) {
            if (other == reservation || other.getSlotNumber() == null
                    || !other.getSlotNumber().equalsIgnoreCase(reservation.getSlotNumber())
                    || !isActiveReservation(other, now)) continue;
            LocalDateTime otherStart = parseReservationDateTime(other.getStartDateTime());
            LocalDateTime otherEnd = parseReservationDateTime(other.getEndDateTime());
            if (otherStart != null && otherEnd != null && windowsOverlap(start, end, otherStart, otherEnd)) return true;
        }
        return false;
    }

    private boolean hasActiveParkingRecordForSlot(String slotNumber) {
        for (ParkingRecord record : records) {
            if (record.getSlotNumber().equalsIgnoreCase(slotNumber) && record.getExitTime() == null) return true;
        }
        return false;
    }

    private boolean hasOtherActiveParkingRecordForSlot(String slotNumber, ParkingRecord exceptRecord) {
        for (ParkingRecord record : records) {
            if (record != exceptRecord && record.getSlotNumber().equalsIgnoreCase(slotNumber)
                    && record.getExitTime() == null) return true;
        }
        return false;
    }

    private ParkingRecord findActiveVisitorRecord(Reservation reservation) {
        for (int i = records.size() - 1; i >= 0; i--) {
            ParkingRecord record = records.get(i);
            if (record.getSlotNumber().equalsIgnoreCase(reservation.getSlotNumber())
                    && reservation.getRequesterId().equals(record.getVisitorId())
                    && reservation.getPlateNumber().equalsIgnoreCase(record.getVehicleId())
                    && record.getExitTime() == null) return record;
        }
        return null;
    }

    public String releaseSlot(String slotNumber) {
        if (currentUser == null) return "Not logged in.";
        UserType role = currentUser.getUserType();
        if (role == UserType.VISITOR) {
            return "Visitors are not allowed to release slots.";
        }

        ParkingSlot slot = findStoredSlot(slotNumber);
        if (!canAccessZone(slot) && role != UserType.PARKING_ADMINISTRATOR) return "Slot not found.";
        if (slot == null) return "Slot not found.";

        if (slot.getAssignedVisitorId() != null) {
            return "Visitor reservations must be completed with the Administrator's departure action.";
        }

        boolean isAdmin = role == UserType.PARKING_ADMINISTRATOR;
        if (!isAdmin && !isHolder(slot, currentUser.getUserId())) {
            return "You can only release your own slot.";
        }

        LocalDateTime now = LocalDateTime.now();
        String holderId = holderIdOf(slot);
        if (holderId != null) {
            endApprovedReservations(slot, holderId, now);
        }

        if (slot.getAssignedStudentId() != null) {
            Student st = findStudentById(slot.getAssignedStudentId());
            if (st != null) st.clearAssignedSlot();
        }
        if (slot.getAssignedPersonnelId() != null) {
            SchoolPersonnel sp = findPersonnelById(slot.getAssignedPersonnelId());
            if (sp != null) {
                sp.setAssignedSlotNumber(null);
                if (slot.getSlotNumber().equalsIgnoreCase(sp.viewReservedSlot())) {
                    sp.clearReservedSlot();
                }
            }
        }

        slot.clearAssignments();
        slot.updateAvailability(true);

        for (int i = records.size() - 1; i >= 0; i--) {
            ParkingRecord rec = records.get(i);
            if (rec.getSlotNumber().equalsIgnoreCase(slot.getSlotNumber()) && rec.getExitTime() == null) {
                rec.recordExit();
                break;
            }
        }
        return "OK";
    }

    private String holderIdOf(ParkingSlot slot) {
        if (slot.getAssignedVisitorId() != null) return slot.getAssignedVisitorId();
        if (slot.getAssignedPersonnelId() != null) return slot.getAssignedPersonnelId();
        return slot.getAssignedStudentId();
    }

    private void endApprovedReservations(ParkingSlot slot, String holderId, LocalDateTime now) {
        for (Reservation r : reservations) {
            if (!"Approved".equalsIgnoreCase(r.getStatus())) continue;
            if (!r.getSlotNumber().equalsIgnoreCase(slot.getSlotNumber())) continue;
            if (!holderId.equals(r.getRequesterId())) continue;

            LocalDateTime start = parseReservationDateTime(r.getStartDateTime());
            if (start != null && !start.isAfter(now)) {
                r.complete();
            } else {
                r.cancel();
            }
        }
    }

    private boolean isHolder(ParkingSlot slot, String userId) {
        if (userId == null) return false;
        return userId.equals(slot.getAssignedStudentId())
                || userId.equals(slot.getAssignedPersonnelId())
                || userId.equals(slot.getAssignedVisitorId());
    }

    private Student findStudentById(String studentId) {
        for (Student st : students.values()) {
            if (st.getStudentId().equals(studentId)) return st;
        }
        return null;
    }

    private SchoolPersonnel findPersonnelById(String personnelId) {
        for (SchoolPersonnel sp : personnelMap.values()) {
            if (sp.getPersonnelId().equals(personnelId)) return sp;
        }
        return null;
    }

    private Visitor findVisitorById(String visitorId) {
            for (Visitor v : visitors.values()) {
            if (v.getVisitorId().equals(visitorId)) return v;
        }
        return null;
    }

    public String getSlotHolderDisplay(ParkingSlot slot) {
        if (slot == null || !canAccessZone(slot)) return "-";
        if (slot.getAssignedStudentId() != null) {
            Student st = findStudentById(slot.getAssignedStudentId());
            String name = st != null ? st.getName() : "Unknown";
            return name + " (" + slot.getAssignedStudentId() + ") - Student";
        }
        if (slot.getAssignedPersonnelId() != null) {
            SchoolPersonnel sp = findPersonnelById(slot.getAssignedPersonnelId());
            String name = sp != null ? sp.getName() : "Unknown";
            return name + " (" + slot.getAssignedPersonnelId() + ") - School Personnel";
        }
        if (slot.getAssignedVisitorId() != null) {
            Visitor v = findVisitorById(slot.getAssignedVisitorId());
            String name = v != null ? v.getName() : "Unknown";
            return name + " (" + slot.getAssignedVisitorId() + ") - Visitor";
        }
        return "-";
    }

    public String addSlot(String slotNumber, String slotType, String zoneType) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can add slots.";
        }
        if (isBlank(slotNumber)) {
            return "Slot number cannot be empty.";
        }
        if (isBlank(slotType)) {
            return "Slot type cannot be empty.";
        }
        if (isBlank(zoneType)) {
            return "Zone type cannot be empty.";
        }
        String normalizedZone = normalizeZoneType(zoneType);
        if (normalizedZone == null) {
            return "Invalid zone type. Allowed zones: Student, School Personnel, Visitor.";
        }
        String normalizedSlotNumber = slotNumber.trim().toUpperCase(Locale.ROOT);
        String namingError = slotNameValidationError(normalizedSlotNumber, normalizedZone);
        if (namingError != null) return namingError;
        if (findStoredSlot(normalizedSlotNumber) != null) {
            return "Slot " + normalizedSlotNumber + " already exists.";
        }
        slots.add(new ParkingSlot(normalizedSlotNumber, slotType.trim(), normalizedZone, true));
        return "OK";
    }

    public String updateSlot(String slotNumber, String slotType, String zoneType) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) return "Only Parking Administrators can edit slots.";
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (isSlotProtected(slot)) return "This slot is occupied, assigned, or involved in an active reservation and cannot be edited.";
        if (isBlank(slotType)) return "Slot type cannot be empty.";
        String normalizedZone = normalizeZoneType(zoneType);
        if (normalizedZone == null) return "Choose Student, School Personnel, or Visitor for the zone.";
        if (!normalizedZone.equalsIgnoreCase(slot.getZoneType())
                && slotNameValidationError(slot.getSlotNumber(), normalizedZone) != null) {
            return "Changing the zone would make the existing slot name inconsistent. Existing slots are not renamed automatically; add a correctly named slot for the new zone.";
        }
        slot.setSlotType(slotType.trim());
        slot.setZoneType(normalizedZone);
        return "OK";
    }

    public String deleteSlot(String slotNumber) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) return "Only Parking Administrators can delete slots.";
        ParkingSlot slot = findStoredSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (isSlotProtected(slot)) return "This slot is occupied, assigned, or involved in an active reservation and cannot be deleted.";
        slots.remove(slot);
        return "OK";
    }

    private boolean isSlotProtected(ParkingSlot slot) {
        if (slot == null) return true;
        if (!slot.checkAvailability() || slot.getAssignedStudentId() != null
                || slot.getAssignedPersonnelId() != null || slot.getAssignedVisitorId() != null
                || slot.getAssignedReservationId() != null
                || hasActiveParkingRecordForSlot(slot.getSlotNumber())) return true;
        LocalDateTime now = LocalDateTime.now();
        for (Reservation reservation : reservations) {
            if (reservation.getSlotNumber() != null
                    && reservation.getSlotNumber().equalsIgnoreCase(slot.getSlotNumber())
                    && isActiveReservation(reservation, now)) return true;
        }
        return false;
    }

    private String slotNameValidationError(String slotNumber, String zoneType) {
        if (isBlank(slotNumber)) return "Slot name cannot be empty.";
        if (!slotNumber.equals(slotNumber.trim())
                || !slotNumber.equals(slotNumber.toUpperCase(Locale.ROOT))) {
            return "Use the uppercase slot-name format with no extra spaces (for example, S-01).";
        }
        String normalizedZone = normalizeZoneType(zoneType);
        if (normalizedZone == null) return "Choose Student, School Personnel, or Visitor for the zone.";
        String prefix = "Student".equals(normalizedZone) ? "S"
                : "School Personnel".equals(normalizedZone) ? "P" : "V";
        if (!slotNumber.matches(prefix + "-(0[1-9]|[1-9][0-9])")) {
            return "Use the correct zone prefix and a two-digit number: " + prefix + "-01, " + prefix + "-02, and so on.";
        }
        return null;
    }

    private String normalizeZoneType(String zoneType) {
        if (zoneType == null) return null;
        String trimmed = zoneType.trim();
        if ("Student".equalsIgnoreCase(trimmed)) return "Student";
        if ("School Personnel".equalsIgnoreCase(trimmed)) return "School Personnel";
        if ("Visitor".equalsIgnoreCase(trimmed)) return "Visitor";
        return null;
    }

    private static final int ADDITIONAL_VEHICLE_FEE = 1500;
    private static final int EXPIRED_RENEWAL_FEE = 1000;
    private static final int MAX_RECEIPT_BYTES = 10 * 1024 * 1024;

    /** Returns the current account's new-registration fee; -1 when the role cannot register vehicles. */
    public int getVehicleRegistrationFee() {
        if (!isVehicleOwnerRole()) return -1;
        int count = 0;
        for (Vehicle vehicle : vehicles) if (currentUser.getUserId().equals(vehicle.getOwnerId())) count++;
        return count == 0 ? 0 : ADDITIONAL_VEHICLE_FEE;
    }

    public int getVehicleRegistrationFee(String plateNumber) {
        if (!isVehicleOwnerRole()) return -1;
        if (!isBlank(plateNumber)) {
            for (Vehicle vehicle : vehicles) {
                if (currentUser.getUserId().equals(vehicle.getOwnerId())
                        && vehicle.getPlateNumber().equalsIgnoreCase(plateNumber.trim())
                        && "REJECTED".equalsIgnoreCase(vehicle.getRegistrationStatus())) {
                    VehicleApplication previous = latestApplicationForVehicle(vehicle.getVehicleId());
                    if (previous != null && "REGISTRATION".equals(previous.getApplicationType())) return previous.getFeeAmount();
                }
            }
        }
        return getVehicleRegistrationFee();
    }

    /** Returns the renewal fee for a vehicle owned by the signed-in user; -1 when unavailable. */
    public int getVehicleRenewalFee(String vehicleId) {
        Vehicle vehicle = findOwnedVehicle(vehicleId);
        if (vehicle == null || !("APPROVED".equalsIgnoreCase(vehicle.getRegistrationStatus())
                || "EXPIRED".equalsIgnoreCase(vehicle.getRegistrationStatus()))) return -1;
        return vehicle.isExpired() ? EXPIRED_RENEWAL_FEE : 0;
    }

    public String submitVehicleRegistration(String fullName, String identificationNumber,
                                            String programOrDepartment, String vehicleType, String plateNumber) {
        if (!isVehicleOwnerRole()) return currentUser == null ? "Not logged in." : "Only Students and School Personnel can register vehicles.";
        if (isBlank(fullName) || isBlank(identificationNumber) || isBlank(programOrDepartment)
                || isBlank(vehicleType) || isBlank(plateNumber)) {
            return "Complete the owner, identification, program/department, vehicle type, and plate fields.";
        }
        String normalizedPlate = plateNumber.trim().toUpperCase();
        Vehicle existingOwned = null;
        for (Vehicle existing : vehicles) {
            if (!existing.getPlateNumber().equalsIgnoreCase(normalizedPlate)) continue;
            if (!currentUser.getUserId().equals(existing.getOwnerId())) return "That plate number is already registered.";
            if (!"REJECTED".equalsIgnoreCase(existing.getRegistrationStatus())) {
                return "This plate is already registered. Choose the vehicle and submit a renewal if needed.";
            }
            existingOwned = existing;
        }

        int fee = getVehicleRegistrationFee();
        if (existingOwned != null) {
            VehicleApplication previous = latestApplicationForVehicle(existingOwned.getVehicleId());
            if (previous != null && "REGISTRATION".equals(previous.getApplicationType())) fee = previous.getFeeAmount();
        }
        String vehicleId;
        Vehicle vehicle;
        if (existingOwned != null) {
            vehicle = existingOwned;
            vehicleId = vehicle.getVehicleId();
            vehicle.setVehicleType(vehicleType.trim());
            vehicle.setRegistrationStatus("PENDING");
            vehicle.setApprovalDate(null);
            vehicle.setExpirationDate(null);
            vehicle.setRejectionReason("");
            vehicle.setPaymentStatus(fee == 0 ? "NOT_REQUIRED" : "NOT_SUBMITTED");
        } else {
            vehicleId = "VEH-" + vehicleCounter++;
            vehicle = new Vehicle(vehicleId, normalizedPlate, vehicleType.trim(), currentUser.getUserType().getLabel(),
                    currentUser.getUserId(), null, "PENDING", null, "", fee == 0 ? "NOT_REQUIRED" : "NOT_SUBMITTED");
            vehicles.add(vehicle);
        }
        VehicleApplication application = new VehicleApplication(nextVehicleApplicationId(), vehicleId,
                currentUser.getUserId(), "REGISTRATION", fullName.trim(), identificationNumber.trim(),
                programOrDepartment.trim(), vehicleType.trim(), normalizedPlate, fee);
        vehicleApplications.add(application);
        return "OK";
    }

    /** Legacy signature retained for source compatibility. New submissions must provide all required registration details. */
    public String registerVehicle(String plateNumber, String vehicleType, String stickerCategory, String ownerId, LocalDate expirationDate) {
        if (currentUser == null) return "Not logged in.";
        if (currentUser.getUserType() == UserType.VISITOR) return "Visitors provide vehicle details when requesting a reservation.";
        return "Use the vehicle registration form and complete the required owner and identification fields.";
    }

    public String submitVehicleRenewal(String vehicleId, String fullName, String identificationNumber,
                                       String programOrDepartment, String vehicleType, String plateNumber) {
        if (!isVehicleOwnerRole()) return currentUser == null ? "Not logged in." : "Only Students and School Personnel can renew vehicles.";
        Vehicle vehicle = findOwnedVehicle(vehicleId);
        if (vehicle == null) return "Vehicle not found or it does not belong to your account.";
        String currentStatus = vehicle.getRegistrationStatus();
        if (!"APPROVED".equalsIgnoreCase(currentStatus) && !"EXPIRED".equalsIgnoreCase(currentStatus)) {
            return "Only an approved or expired vehicle can be renewed.";
        }
        if (isBlank(fullName) || isBlank(identificationNumber) || isBlank(programOrDepartment)
                || isBlank(vehicleType) || isBlank(plateNumber)) {
            return "Complete all required renewal form fields.";
        }
        for (VehicleApplication existing : vehicleApplications) {
            if (existing.getVehicleId().equals(vehicleId)
                    && "RENEWAL".equals(existing.getApplicationType())
                    && "PENDING".equalsIgnoreCase(existing.getStatus())) {
                return "A renewal for this vehicle is already awaiting review.";
            }
        }
        String normalizedPlate = plateNumber.trim().toUpperCase();
        for (Vehicle other : vehicles) {
            if (!other.getVehicleId().equals(vehicleId) && other.getPlateNumber().equalsIgnoreCase(normalizedPlate)) {
                return "That plate number is already registered.";
            }
        }
        int fee = vehicle.isExpired() ? EXPIRED_RENEWAL_FEE : 0;
        vehicleApplications.add(new VehicleApplication(nextVehicleApplicationId(), vehicleId,
                currentUser.getUserId(), "RENEWAL", fullName.trim(), identificationNumber.trim(),
                programOrDepartment.trim(), vehicleType.trim(), normalizedPlate, fee));
        return "OK";
    }

    private boolean isVehicleOwnerRole() {
        return currentUser != null && (currentUser.getUserType() == UserType.STUDENT
                || currentUser.getUserType() == UserType.SCHOOL_PERSONNEL);
    }

    private String nextVehicleApplicationId() { return "VAPP-" + vehicleApplicationCounter++; }

    private Vehicle findOwnedVehicle(String vehicleId) {
        if (!isVehicleOwnerRole() || isBlank(vehicleId)) return null;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equalsIgnoreCase(vehicleId)
                    && currentUser.getUserId().equals(vehicle.getOwnerId())) return vehicle;
        }
        return null;
    }

    private Vehicle findStoredVehicle(String vehicleId) {
        if (isBlank(vehicleId)) return null;
        for (Vehicle vehicle : vehicles) if (vehicle.getVehicleId().equalsIgnoreCase(vehicleId)) return vehicle;
        return null;
    }

    private VehicleApplication findStoredApplication(String applicationId) {
        if (isBlank(applicationId)) return null;
        for (VehicleApplication application : vehicleApplications) {
            if (application.getApplicationId().equalsIgnoreCase(applicationId)) return application;
        }
        return null;
    }

    private VehicleApplication latestApplicationForVehicle(String vehicleId) {
        VehicleApplication latest = null;
        for (VehicleApplication application : vehicleApplications) {
            if (!application.getVehicleId().equals(vehicleId)) continue;
            if (latest == null || application.getSubmittedAt().isAfter(latest.getSubmittedAt())) latest = application;
        }
        return latest;
    }

    private boolean canManageApplication(VehicleApplication application) {
        return application != null && currentUser != null
                && (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                || currentUser.getUserId().equals(application.getOwnerId()));
    }

    public List<VehicleApplication> getVehicleApplications() {
        if (currentUser == null || currentUser.getUserType() == UserType.VISITOR) return Collections.emptyList();
        List<VehicleApplication> visible = new ArrayList<>();
        for (VehicleApplication application : vehicleApplications) {
            if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                    || currentUser.getUserId().equals(application.getOwnerId())) visible.add(application.copy());
        }
        return Collections.unmodifiableList(visible);
    }

    public VehicleApplication getLatestVehicleApplication(String vehicleId) {
        Vehicle vehicle = findStoredVehicle(vehicleId);
        if (vehicle == null || currentUser == null
                || (currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR
                && !currentUser.getUserId().equals(vehicle.getOwnerId()))) return null;
        VehicleApplication latest = latestApplicationForVehicle(vehicleId);
        return latest == null ? null : latest.copy();
    }

    public String submitVehiclePayment(String applicationId, String paymentMethod,
                                       String referenceNumber, byte[] receiptImage) {
        VehicleApplication application = findStoredApplication(applicationId);
        if (!canManageApplication(application) || currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR) {
            return "Application not found or you do not have permission to submit payment.";
        }
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) return "This application is no longer pending.";
        if (application.getFeeAmount() <= 0) return "This application does not require payment.";
        if (!"NOT_SUBMITTED".equalsIgnoreCase(application.getPaymentStatus())
                && !"REJECTED".equalsIgnoreCase(application.getPaymentStatus())) {
            return "Payment is already awaiting verification or has been verified.";
        }
        if (!List.of("GCash", "Maya", "Bank Transfer", "Cash/Cashier").contains(paymentMethod)) {
            return "Choose GCash, Maya, Bank Transfer, or Cash/Cashier.";
        }
        if (isBlank(referenceNumber)) return "Enter the transaction reference number or OR number.";
        if (receiptImage == null || receiptImage.length == 0) return "A receipt image is required.";
        if (receiptImage.length > MAX_RECEIPT_BYTES) return "The receipt image must be 10 MB or smaller.";
        try {
            if (ImageIO.read(new ByteArrayInputStream(receiptImage)) == null) return "Choose a valid PNG or JPEG receipt image.";
        } catch (IOException ex) {
            return "The receipt image could not be read. Choose another image.";
        }
        application.setPayment(paymentMethod, referenceNumber.trim(), receiptImage);
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle != null) vehicle.setPaymentStatus("PENDING_VERIFICATION");
        return "OK";
    }

    public String verifyVehiclePayment(String applicationId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can verify payments.";
        VehicleApplication application = findStoredApplication(applicationId);
        if (application == null) return "Application not found.";
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) return "This application is no longer pending.";
        if (!"PENDING_VERIFICATION".equalsIgnoreCase(application.getPaymentStatus())) return "No payment is awaiting verification.";
        application.setPaymentStatus("VERIFIED");
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle != null) vehicle.setPaymentStatus("VERIFIED");
        return "OK";
    }

    public String rejectVehiclePayment(String applicationId, String reason) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can reject payments.";
        VehicleApplication application = findStoredApplication(applicationId);
        if (application == null) return "Application not found.";
        if (!"PENDING_VERIFICATION".equalsIgnoreCase(application.getPaymentStatus())) return "No payment is awaiting verification.";
        if (isBlank(reason)) return "Enter a reason so the owner can correct and resubmit the payment.";
        application.setPaymentStatus("REJECTED");
        application.setPaymentRejectionReason(reason.trim());
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle != null) vehicle.setPaymentStatus("REJECTED");
        return "OK";
    }

    public byte[] getVehiclePaymentProof(String applicationId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return null;
        VehicleApplication application = findStoredApplication(applicationId);
        return application == null ? null : application.getReceiptImage();
    }

    public String approveVehicleApplication(String applicationId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can approve vehicle applications.";
        VehicleApplication application = findStoredApplication(applicationId);
        if (application == null) return "Application not found.";
        if ("STUDENT_INITIAL_REGISTRATION".equals(application.getApplicationType())) {
            return "Approve the Student account and initial vehicle together from Registered Users.";
        }
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) return "This application is already " + application.getStatus() + ".";
        if (application.getFeeAmount() > 0 && !"VERIFIED".equalsIgnoreCase(application.getPaymentStatus())) {
            return "Verify the required payment before approving this application.";
        }
        Vehicle vehicle = findStoredVehicle(application.getVehicleId());
        if (vehicle == null) return "Vehicle record not found.";
        if ("REGISTRATION".equals(application.getApplicationType())
                && !"PENDING".equalsIgnoreCase(vehicle.getRegistrationStatus())) return "Vehicle registration is not pending.";
        if ("RENEWAL".equals(application.getApplicationType())
                && !"APPROVED".equalsIgnoreCase(vehicle.getRegistrationStatus())
                && !"EXPIRED".equalsIgnoreCase(vehicle.getRegistrationStatus())) return "Only an approved or expired vehicle can be renewed.";

        vehicle.setPlateNumber(application.getPlateNumber());
        vehicle.setVehicleType(application.getVehicleType());
        vehicle.setRegistrationStatus("APPROVED");
        vehicle.setApprovalDate(LocalDate.now());
        vehicle.setExpirationDate(LocalDate.now().plusMonths(5));
        vehicle.setRejectionReason("");
        vehicle.setPaymentStatus(application.getFeeAmount() == 0 ? "NOT_REQUIRED" : "VERIFIED");
        application.setStatus("APPROVED");
        vehicleReminders.removeIf(reminder -> reminder.getVehicleId().equals(vehicle.getVehicleId()));
        return "OK";
    }

    public String rejectVehicleApplication(String applicationId, String reason) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can reject vehicle applications.";
        VehicleApplication application = findStoredApplication(applicationId);
        if (application == null) return "Application not found.";
        if ("STUDENT_INITIAL_REGISTRATION".equals(application.getApplicationType())) {
            return "Reject the Student account and initial vehicle together from Registered Users.";
        }
        if (!"PENDING".equalsIgnoreCase(application.getStatus())) return "This application is already " + application.getStatus() + ".";
        if (isBlank(reason)) return "A rejection reason is required.";
        application.setStatus("REJECTED");
        application.setRejectionReason(reason.trim());
        if ("REGISTRATION".equals(application.getApplicationType())) {
            Vehicle vehicle = findStoredVehicle(application.getVehicleId());
            if (vehicle != null) {
                vehicle.setRegistrationStatus("REJECTED");
                vehicle.setRejectionReason(reason.trim());
            }
        }
        return "OK";
    }

    public String remindVehicleExpiry(String vehicleId) {
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only Parking Administrators can send expiration reminders.";
        Vehicle vehicle = findStoredVehicle(vehicleId);
        if (vehicle == null || !vehicle.isApprovedAndValid()) return "Only an approved, unexpired vehicle can receive an expiration reminder.";
        long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), vehicle.getExpirationDate());
        if (daysUntilExpiry < 0 || daysUntilExpiry > 7) return "Reminders can be sent during the seven days before expiration.";
        LocalDate today = LocalDate.now();
        for (VehicleReminder reminder : vehicleReminders) {
            if (reminder.isManual() && reminder.getVehicleId().equals(vehicleId)
                    && reminder.getCreatedAt().toLocalDate().equals(today)) return "A reminder was already sent today.";
        }
        String message = "Registration for " + vehicle.getPlateNumber() + " expires on " + vehicle.getExpirationDate() + ". Please submit a renewal.";
        vehicleReminders.add(new VehicleReminder(vehicleId, vehicle.getOwnerId(), vehicle.getPlateNumber(), message, true));
        return "OK";
    }

    public String getVehicleReminderMessage() {
        if (currentUser == null || currentUser.getUserType() == UserType.VISITOR) return null;
        processAutomaticVehicleReminders();
        for (int i = vehicleReminders.size() - 1; i >= 0; i--) {
            VehicleReminder reminder = vehicleReminders.get(i);
            if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                    || currentUser.getUserId().equals(reminder.getOwnerId())) return reminder.getMessage();
        }
        return null;
    }

    private void processAutomaticVehicleReminders() {
        LocalDate today = LocalDate.now();
        for (Vehicle vehicle : vehicles) {
            if (!vehicle.isApprovedAndValid() || vehicle.getExpirationDate() == null) continue;
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, vehicle.getExpirationDate());
            if (daysUntilExpiry < 0 || daysUntilExpiry > 7 || hasReminderForVehicle(vehicle.getVehicleId())) continue;
            String message = "Registration for " + vehicle.getPlateNumber() + " expires in " + daysUntilExpiry
                    + " day" + (daysUntilExpiry == 1 ? "" : "s") + " on " + vehicle.getExpirationDate() + ". Please submit a renewal.";
            vehicleReminders.add(new VehicleReminder(vehicle.getVehicleId(), vehicle.getOwnerId(), vehicle.getPlateNumber(), message, false));
        }
    }

    private boolean hasReminderForVehicle(String vehicleId) {
        for (VehicleReminder reminder : vehicleReminders) {
            if (reminder.getVehicleId().equals(vehicleId)) return true;
        }
        return false;
    }

    private String validateVehicleAccessForParking() {
        if (!isVehicleOwnerRole()) return "Only Students and School Personnel can park through this action.";
        if (isCurrentUser(UserType.STUDENT)) {
            UserAccount account = findAccount(currentUser.getName());
            if (account != null && "PENDING".equalsIgnoreCase(account.getRegistrationStatus())) {
                return "Your Student registration is awaiting Parking Administrator approval.";
            }
            if (account != null && "REJECTED".equalsIgnoreCase(account.getRegistrationStatus())) {
                return "Your Student registration was rejected. Administrator reason: "
                        + account.getRegistrationRejectionReason();
            }
        }
        boolean hasVehicle = false;
        boolean expired = false;
        boolean pending = false;
        String rejection = null;
        for (Vehicle vehicle : vehicles) {
            if (!currentUser.getUserId().equals(vehicle.getOwnerId())) continue;
            hasVehicle = true;
            if (vehicle.isApprovedAndValid()) return null;
            if (vehicle.isExpired()) expired = true;
            if ("PENDING".equalsIgnoreCase(vehicle.getRegistrationStatus())) pending = true;
            if ("REJECTED".equalsIgnoreCase(vehicle.getRegistrationStatus())) rejection = vehicle.getRejectionReason();
        }
        if (!hasVehicle) return "Register a vehicle and wait for Administrator approval before using parking slots.";
        if (expired) return "Your vehicle registration has expired. Submit a renewal and wait for Administrator approval before parking.";
        if (pending) return "Your vehicle registration is pending Administrator approval. You cannot park until it is approved.";
        if (rejection != null && !rejection.isBlank()) return "Your vehicle registration was rejected: " + rejection;
        return "An approved, unexpired vehicle is required before using parking slots.";
    }

    public boolean hasApprovedVehicleForCurrentUser() {
        if (!isVehicleOwnerRole()) return false;
        if (isCurrentUser(UserType.STUDENT)) {
            UserAccount account = findAccount(currentUser.getName());
            if (account == null || !"APPROVED".equalsIgnoreCase(account.getRegistrationStatus())) return false;
        }
        for (Vehicle vehicle : vehicles) {
            if (currentUser.getUserId().equals(vehicle.getOwnerId()) && vehicle.isApprovedAndValid()) return true;
        }
        return false;
    }

    /** An expired owner with an active session may still view the session to release it. */
    public boolean canAccessParkingSlotsForCurrentUser() {
        if (isCurrentUser(UserType.STUDENT)) {
            UserAccount account = findAccount(currentUser.getName());
            if (account == null || !"APPROVED".equalsIgnoreCase(account.getRegistrationStatus())) return false;
        }
        if (hasApprovedVehicleForCurrentUser()) return true;
        return (isCurrentUser(UserType.STUDENT) || isCurrentUser(UserType.SCHOOL_PERSONNEL))
                && getAssignedSlotForCurrentUser() != null;
    }

    public String getVehicleAccessMessageForCurrentUser() {
        return validateVehicleAccessForParking();
    }

    public List<Vehicle> getVehicles() {
        if (currentUser == null || currentUser.getUserType() == UserType.VISITOR) return Collections.emptyList();
        List<Vehicle> visible = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                    || currentUser.getUserId().equals(vehicle.getOwnerId())) {
                visible.add(vehicle.copy());
            }
        }
        return Collections.unmodifiableList(visible);
    }

    public List<Vehicle> getVehiclesForCurrentUser() {
        return getVehicles();
    }

    public String updateVehicle(String vehicleId, String plate, String type, LocalDate expiry) {
        Vehicle vehicle = findEditableVehicle(vehicleId);
        if (vehicle == null) return "Vehicle not found or you do not have permission to edit it.";
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Vehicle details can be changed only by the Parking Administrator.";
        if (!"APPROVED".equalsIgnoreCase(vehicle.getRegistrationStatus()) || hasPendingVehicleApplication(vehicleId)) {
            return "Pending, rejected, or expiring vehicle details must be corrected through registration or renewal review.";
        }
        if (isBlank(plate) || isBlank(type) || expiry == null || !expiry.equals(vehicle.getExpirationDate())) {
            return "Edit the plate or vehicle type only. Expiration dates can change only after an approved renewal.";
        }
        for (Vehicle other : vehicles) if (!other.getVehicleId().equals(vehicleId) && other.getPlateNumber().equalsIgnoreCase(plate.trim())) return "That plate number is already registered.";
        vehicle.setPlateNumber(plate.trim().toUpperCase());
        vehicle.setVehicleType(type.trim());
        return "OK";
    }

    public String deleteVehicle(String vehicleId) {
        Vehicle vehicle = findEditableVehicle(vehicleId);
        if (vehicle == null) return "Vehicle not found or you do not have permission to delete it.";
        if (!isCurrentUser(UserType.PARKING_ADMINISTRATOR)) return "Only the Parking Administrator can remove vehicle records.";
        for (VehicleApplication application : vehicleApplications) {
            if (application.getVehicleId().equals(vehicleId)
                    && ("PENDING".equalsIgnoreCase(application.getStatus())
                    || "PENDING_VERIFICATION".equalsIgnoreCase(application.getPaymentStatus())
                    || "REJECTED".equalsIgnoreCase(application.getPaymentStatus()))) {
                return "This vehicle has an unresolved registration or payment review and cannot be removed.";
            }
        }
        vehicles.remove(vehicle);
        return "OK";
    }

    private boolean hasPendingVehicleApplication(String vehicleId) {
        for (VehicleApplication application : vehicleApplications) {
            if (application.getVehicleId().equals(vehicleId) && "PENDING".equalsIgnoreCase(application.getStatus())) return true;
        }
        return false;
    }

    public String getVehicleOwnerName(String vehicleId) {
        Vehicle vehicle = findStoredVehicle(vehicleId);
        if (vehicle == null || currentUser == null
                || (currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR
                && !currentUser.getUserId().equals(vehicle.getOwnerId()))) return "-";
        for (UserAccount account : accounts.values()) if (account.getUserId().equals(vehicle.getOwnerId())) return account.getFullName();
        return "Unknown";
    }

    private Vehicle findEditableVehicle(String vehicleId) {
        if (currentUser == null || currentUser.getUserType() == UserType.VISITOR || isBlank(vehicleId)) return null;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equalsIgnoreCase(vehicleId)
                    && (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR || currentUser.getUserId().equals(vehicle.getOwnerId()))) return vehicle;
        }
        return null;
    }

    public List<ParkingRecord> getRecords() {
        if (currentUser == null) return Collections.emptyList();
        List<ParkingRecord> visible = new ArrayList<>();
        for (ParkingRecord record : records) {
            if (currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR
                    || currentUser.getUserId().equals(record.getVisitorId())) {
                visible.add(record.copy());
            }
        }
        return Collections.unmodifiableList(visible);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
