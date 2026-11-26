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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingService {

    private static final DateTimeFormatter RESERVATION_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

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

        slots.add(new ParkingSlot("V-01", "Standard", "Visitor", true));
        slots.add(new ParkingSlot("V-02", "Standard", "Visitor", true));

        vehicles.add(new Vehicle("VEH-1", "ABC-1234", "Sedan", "Student", "student1", LocalDate.now().plusMonths(6)));
        vehicles.add(new Vehicle("VEH-2", "XYZ-9876", "SUV", "School Personnel", "staff1", LocalDate.now().plusMonths(10)));

        UserAccount visitorAcc = accounts.get("visitor1");
        LocalDate sampleDay = LocalDate.now().plusDays(1);
        Reservation sampleRes = new Reservation(
                "RES-" + (reservationCounter++),
                visitorAcc != null ? visitorAcc.getUserId() : "VIS-104",
                "visitor1",
                UserType.VISITOR,
                "V-02",
                sampleDay + " 09:00",
                sampleDay + " 12:00",
                "Pending"
        );
        reservations.add(sampleRes);

        ParkingRecord sampleRec = new ParkingRecord("REC-" + (recordCounter++), "VEH-1", "S-03", null);
        records.add(sampleRec);
    }

    public boolean registerAccount(String name, String password, UserType userType) {
        if (isBlank(name) || isBlank(password) || userType == null) {
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
        if (isBlank(name) || isBlank(password) || expectedType == null) {
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
        return Collections.unmodifiableList(slots);
    }

    public ParkingSlot findSlot(String slotNumber) {
        if (isBlank(slotNumber)) return null;
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

        if (!st.selectSlot(slot)) {
            return "Unable to assign slot " + slot.getSlotNumber() + ".";
        }

        records.add(new ParkingRecord("REC-" + (recordCounter++), "STUDENT-VEHICLE", slot.getSlotNumber(), null));
        return "OK";
    }

    public String selectSlotForPersonnel(String slotNumber) {
        if (currentUser == null || currentUser.getUserType() != UserType.SCHOOL_PERSONNEL) {
            return "Only School Personnel can select slots for immediate use.";
        }
        SchoolPersonnel sp = personnelMap.get(currentUser.getName().toLowerCase());
        if (sp == null) return "Personnel profile not found.";
        if (sp.viewAssignedSlot() != null) {
            return "You already have an active slot (" + sp.viewAssignedSlot() + ").";
        }
        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (!"School Personnel".equalsIgnoreCase(slot.getZoneType())) {
            return "School Personnel can only select School Personnel zone slots.";
        }
        if (!slot.checkAvailability()) {
            return "Slot " + slot.getSlotNumber() + " is not available.";
        }

        if (!sp.selectSlot(slot)) {
            return "Unable to assign slot " + slot.getSlotNumber() + ".";
        }

        records.add(new ParkingRecord("REC-" + (recordCounter++), "PERSONNEL-VEHICLE", slot.getSlotNumber(), null));
        return "OK";
    }

    public String requestReservation(String slotNumber, String startDateTime, String endDateTime) {
        if (currentUser == null) return "Not logged in.";
        UserType role = currentUser.getUserType();
        if (role != UserType.VISITOR && role != UserType.SCHOOL_PERSONNEL) {
            return "Only Visitors and School Personnel can request reservations.";
        }
        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";
        if (role == UserType.VISITOR && !"Visitor".equalsIgnoreCase(slot.getZoneType())) {
            return "Visitors can only request Visitor zone slots.";
        }
        if (role == UserType.SCHOOL_PERSONNEL && !"School Personnel".equalsIgnoreCase(slot.getZoneType())) {
            return "School Personnel can only request School Personnel zone slots.";
        }
        if (role == UserType.SCHOOL_PERSONNEL) {
            SchoolPersonnel holder = personnelMap.get(currentUser.getName().toLowerCase());
            if (holder != null && holder.viewAssignedSlot() != null) {
                return "You already have an assigned slot (" + holder.viewAssignedSlot()
                        + "). Release it before requesting a reservation.";
            }
        }
        if (!slot.checkAvailability()) {
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

        if (role == UserType.VISITOR) {
            Visitor v = visitors.get(currentUser.getName().toLowerCase());
            if (v != null) v.requestReservation(slot.getSlotNumber());
        }

        Reservation res = new Reservation(
                "RES-" + (reservationCounter++),
                currentUser.getUserId(),
                currentUser.getName(),
                role,
                slot.getSlotNumber(),
                startDateTime.trim(),
                endDateTime.trim(),
                "Pending"
        );
        reservations.add(res);
        return "OK";
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
        LocalDateTime newStart = parseReservationDateTime(startDateTime);
        LocalDateTime newEnd = parseReservationDateTime(endDateTime);
        if (newStart == null || newEnd == null) {
            return "Invalid date/time format. Use yyyy-MM-dd HH:mm (e.g., 2026-11-27 08:00).";
        }

        String requesterId = currentUser.getUserId();
        LocalDateTime now = LocalDateTime.now();

        for (Reservation existing : reservations) {
            if (!isActiveReservation(existing, now)) continue;

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
        boolean pendingOrApproved = "Pending".equalsIgnoreCase(r.getStatus()) || "Approved".equalsIgnoreCase(r.getStatus());
        if (!pendingOrApproved) return false;
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
                r.approve();
                slot.updateAvailability(false);
                if (r.getRequesterRole() == UserType.VISITOR) {
                    slot.setAssignedVisitorId(r.getRequesterId());
                } else {
                    slot.setAssignedPersonnelId(r.getRequesterId());
                    SchoolPersonnel sp = personnelMap.get(r.getRequesterName().toLowerCase());
                    if (sp != null) sp.reservedSlot(slot.getSlotNumber());
                }
                String vehicleLabel = r.getRequesterRole() == UserType.VISITOR ? "VISITOR-VEHICLE" : "PERSONNEL-VEHICLE";
                ParkingRecord rec = new ParkingRecord("REC-" + (recordCounter++), vehicleLabel, slot.getSlotNumber(), r.getRequesterId());
                rec.markVisitorIdSurrendered(r.getRequesterRole() == UserType.VISITOR);
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
        UserType role = currentUser.getUserType();
        if (role == UserType.VISITOR) {
            return "Visitors are not allowed to release slots.";
        }

        ParkingSlot slot = findSlot(slotNumber);
        if (slot == null) return "Slot not found.";

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
        if (slot == null) return "-";
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
        if (findSlot(slotNumber) != null) {
            return "Slot " + slotNumber.trim().toUpperCase() + " already exists.";
        }
        slots.add(new ParkingSlot(slotNumber.trim().toUpperCase(), slotType.trim(), normalizedZone, true));
        return "OK";
    }

    private String normalizeZoneType(String zoneType) {
        String trimmed = zoneType.trim();
        if ("Student".equalsIgnoreCase(trimmed)) return "Student";
        if ("School Personnel".equalsIgnoreCase(trimmed)) return "School Personnel";
        if ("Visitor".equalsIgnoreCase(trimmed)) return "Visitor";
        return null;
    }

    public String registerVehicle(String plateNumber, String vehicleType, String stickerCategory, String ownerId, LocalDate expirationDate) {
        if (currentUser == null || currentUser.getUserType() != UserType.PARKING_ADMINISTRATOR) {
            return "Only Parking Administrators can register vehicles.";
        }
        if (isBlank(plateNumber)) {
            return "Plate number is required.";
        }
        if (isBlank(vehicleType)) {
            return "Vehicle type is required.";
        }
        if (isBlank(stickerCategory)) {
            return "Sticker category is required.";
        }
        if (isBlank(ownerId)) {
            return "Owner ID is required.";
        }
        if (expirationDate == null) {
            return "Expiration date is required.";
        }
        if (expirationDate.isBefore(LocalDate.now())) {
            return "Expiration date cannot be in the past.";
        }
        vehicles.add(new Vehicle(
                "VEH-" + (vehicleCounter++),
                plateNumber.trim().toUpperCase(),
                vehicleType.trim(),
                stickerCategory.trim(),
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

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}