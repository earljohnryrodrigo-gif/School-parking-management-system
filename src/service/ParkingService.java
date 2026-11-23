package service;

import model.ParkingRecord;
import model.ParkingSlot;
import model.Reservation;
import model.SchoolPersonnel;
import model.Student;
import model.UserType;
import model.Vehicle;
import model.Visitor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingService {

    public static class UserAccount {
        private final String userId;
        private final String name;
        private final String password;
        private final UserType userType;

        public UserAccount(String userId, String name, String password, UserType userType) {
            this.userId = userId;
            this.name = name;
            this.password = password;
            this.userType = userType;
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
    }

    private final Map<String, UserAccount> accounts = new HashMap<>();
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, SchoolPersonnel> personnelMap = new HashMap<>();
    private final Map<String, Visitor> visitors = new HashMap<>();

    private final List<ParkingSlot> slots = new ArrayList<>();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<ParkingRecord> records = new ArrayList<>();

    private UserAccount currentUser;
    private int userCounter = 100;
    private int reservationCounter = 1;
    private int recordCounter = 1;
    private int vehicleCounter = 1;

    public ParkingService() {
        seedInitialData();
    }

    private void seedInitialData() {
        // Default demo accounts
        registerAccount("admin", "1234", UserType.PARKING_ADMINISTRATOR);
        registerAccount("student1", "1234", UserType.STUDENT);
        registerAccount("staff1", "1234", UserType.SCHOOL_PERSONNEL);
        registerAccount("visitor1", "1234", UserType.VISITOR);

        // Student slots
        slots.add(new ParkingSlot("S-01", "Standard", "Student", true));
        slots.add(new ParkingSlot("S-02", "Standard", "Student", true));
        ParkingSlot s03 = new ParkingSlot("S-03", "Standard", "Student", false);
        s03.setAssignedStudentId("ST-102");
        slots.add(s03);
        slots.add(new ParkingSlot("S-04", "Motorcycle", "Student", true));

        // School Personnel slots (Option B: assigned permanent slots)
        ParkingSlot p01 = new ParkingSlot("P-01", "Standard", "School Personnel", false);
        p01.setAssignedPersonnelId("staff1");
        slots.add(p01);

        SchoolPersonnel staff1 = personnelMap.get("staff1");
        if (staff1 != null) {
            staff1.setAssignedSlotNumber("P-01");
        }

        slots.add(new ParkingSlot("P-02", "Standard", "School Personnel", true));
        slots.add(new ParkingSlot("P-03", "Standard", "School Personnel", true));

        // Visitor slots
        slots.add(new ParkingSlot("V-01", "Standard", "Visitor", true));
        slots.add(new ParkingSlot("V-02", "Standard", "Visitor", true));

        // Sample vehicle registrations
        vehicles.add(new Vehicle("VEH-1", "ABC-1234", "Sedan", "Student", "student1", LocalDate.now().plusMonths(6)));
        vehicles.add(new Vehicle("VEH-2", "XYZ-9876", "SUV", "School Personnel", "staff1", LocalDate.now().plusMonths(10)));

        // Sample pending visitor reservation
        Reservation sampleRes = new Reservation(
                "RES-" + (reservationCounter++),
                "VIS-104",
                "visitor1",
                UserType.VISITOR,
                "V-02",
                "2026-11-21 09:00",
                "2026-11-21 12:00",
                "Pending"
        );
        reservations.add(sampleRes);

        // Sample entry record
        ParkingRecord sampleRec = new ParkingRecord("REC-" + (recordCounter++), "VEH-1", "S-03", null);
        records.add(sampleRec);
    }

    public boolean registerAccount(String name, String password, UserType userType) {
        if (name == null || name.trim().isEmpty() || password == null || password.isEmpty() || userType == null) {
            return false;
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

        UserAccount acc = new UserAccount(id, cleanName, password, userType);
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
        if (name == null || password == null || expectedType == null) {
            return null;
        }
        UserAccount acc = accounts.get(name.trim().toLowerCase());
        if (acc != null && acc.getPassword().equals(password) && acc.getUserType() == expectedType) {
            this.currentUser = acc;
            return acc;
        }
        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public UserAccount getCurrentUser() {
        return currentUser;
    }

    public List<ParkingSlot> getAllSlots() {
        return slots;
    }

    public ParkingSlot findSlot(String slotNumber) {
        if (slotNumber == null) return null;
        for (ParkingSlot s : slots) {
            if (s.getSlotNumber().equalsIgnoreCase(slotNumber.trim())) {
                return s;
            }
        }
        return null;
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

    // Option B: Student selects an available Student slot for immediate use
    public String selectSlotForStudent(String slotNumber) {
        if (currentUser == null || currentUser.getUserType() != UserType.STUDENT) {
            return "Only Students can select slots for immediate use.";
        }
        Student st = students.get(currentUser.getName().toLowerCase());
        if (st == null) return "Student profile not found.";
        if (st.viewAssignedSlot() != null) {
            return "You already have an active slot (" + st.viewAssignedSlot() + ").";
        }
        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (!"Student".equalsIgnoreCase(slot.getZoneType())) {
            return "Students can only select Student zone slots.";
        }
        if (!slot.checkAvailability()) {
            return "Slot " + slot.getSlotNumber() + " is not available.";
        }

        slot.updateAvailability(false);
        slot.setAssignedStudentId(currentUser.getName());
        st.selectSlot(slot);

        records.add(new ParkingRecord("REC-" + (recordCounter++), "STUDENT-VEHICLE", slot.getSlotNumber(), null));
        return "OK";
    }

    // Option B: Visitor (or Personnel) requests a reservation for admin approval
    public String requestReservation(String slotNumber, String startDateTime, String endDateTime) {
        if (currentUser == null) return "Not logged in.";
        if (currentUser.getUserType() != UserType.VISITOR && currentUser.getUserType() != UserType.SCHOOL_PERSONNEL) {
            return "Only Visitors and School Personnel can request reservations.";
        }
        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (currentUser.getUserType() == UserType.VISITOR && !"Visitor".equalsIgnoreCase(slot.getZoneType())) {
            return "Visitors can only request Visitor zone slots.";
        }
        if (!slot.checkAvailability()) {
            return "Slot " + slot.getSlotNumber() + " is currently not available.";
        }

        if (currentUser.getUserType() == UserType.VISITOR) {
            Visitor v = visitors.get(currentUser.getName().toLowerCase());
            if (v != null) v.requestReservation(slot.getSlotNumber());
        } else {
            SchoolPersonnel sp = personnelMap.get(currentUser.getName().toLowerCase());
            if (sp != null) sp.reservedSlot(slot.getSlotNumber());
        }

        Reservation res = new Reservation(
                "RES-" + (reservationCounter++),
                currentUser.getUserId(),
                currentUser.getName(),
                currentUser.getUserType(),
                slot.getSlotNumber(),
                startDateTime,
                endDateTime,
                "Pending"
        );
        reservations.add(res);
        return "OK";
    }

    public List<Reservation> getReservations() {
        return reservations;
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
                ParkingSlot slot = findSlot(r.getSlotNumber());
                if (slot == null || !slot.checkAvailability()) {
                    return "Target slot is no longer available.";
                }
                r.approve();
                slot.updateAvailability(false);
                if (r.getRequesterRole() == UserType.VISITOR) {
                    slot.setAssignedVisitorId(r.getRequesterName());
                } else {
                    slot.setAssignedPersonnelId(r.getRequesterName());
                }
                ParkingRecord rec = new ParkingRecord("REC-" + (recordCounter++), "VISITOR-VEHICLE", slot.getSlotNumber(), r.getRequesterId());
                rec.markVisitorIdSurrendered(true);
                records.add(rec);
                return "OK";
            }
        }
        return "Reservation ID not found.";
    }

    public String rejectReservation(String reservationId) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can reject reservations.";
        }
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(reservationId)) {
                if (!"Pending".equalsIgnoreCase(r.getStatus())) {
                    return "Reservation is already " + r.getStatus() + ".";
                }
                r.reject();
                return "OK";
            }
        }
        return "Reservation ID not found.";
    }

    public String releaseSlot(String slotNumber) {
        if (currentUser == null) return "Not logged in.";
        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";

        boolean isAdmin = currentUser.getUserType() == UserType.PARKING_ADMINISTRATOR;
        String holder = slot.getAssignedHolderLabel();
        if (!isAdmin && !holder.toLowerCase().startsWith(currentUser.getName().toLowerCase())) {
            return "You can only release your own slot.";
        }

        if (slot.getAssignedStudentId() != null) {
            Student st = students.get(slot.getAssignedStudentId().toLowerCase());
            if (st != null) st.clearAssignedSlot();
        }
        if (slot.getAssignedPersonnelId() != null) {
            SchoolPersonnel sp = personnelMap.get(slot.getAssignedPersonnelId().toLowerCase());
            if (sp != null) sp.setAssignedSlotNumber(null);
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

    public String addSlot(String slotNumber, String slotType, String zoneType) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can add slots.";
        }
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            return "Slot number cannot be empty.";
        }
        if (findSlot(slotNumber) != null) {
            return "Slot " + slotNumber.toUpperCase() + " already exists.";
        }
        slots.add(new ParkingSlot(slotNumber.trim().toUpperCase(), slotType, zoneType, true));
        return "OK";
    }

    public String registerVehicle(String plateNumber, String vehicleType, String stickerCategory, String ownerId, LocalDate expirationDate) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can register vehicles.";
        }
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return "Plate number is required.";
        }
        vehicles.add(new Vehicle(
                "VEH-" + (vehicleCounter++),
                plateNumber.trim().toUpperCase(),
                vehicleType,
                stickerCategory,
                ownerId.trim(),
                expirationDate
        ));
        return "OK";
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<ParkingRecord> getRecords() {
        return records;
    }
}