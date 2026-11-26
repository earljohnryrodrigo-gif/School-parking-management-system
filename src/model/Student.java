package model;

public class Student {
    private final String studentId;
    private final String name;
    private final String password;
    private String assignedSlotNumber;

    public Student(String studentId, String name, String password) {
        this.studentId = studentId;
        this.name = name;
        this.password = password;
        this.assignedSlotNumber = null;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String viewAssignedSlot() {
        return assignedSlotNumber;
    }

    public boolean selectSlot(ParkingSlot slot) {
        if (assignedSlotNumber != null || slot == null) {
            return false;
        }
        if (!"Student".equalsIgnoreCase(slot.getZoneType()) || !slot.checkAvailability()) {
            return false;
        }
        slot.updateAvailability(false);
        slot.setAssignedStudentId(studentId);
        this.assignedSlotNumber = slot.getSlotNumber();
        return true;
    }

    public void setAssignedSlotNumber(String assignedSlotNumber) {
        this.assignedSlotNumber = assignedSlotNumber;
    }

    public void clearAssignedSlot() {
        this.assignedSlotNumber = null;
    }
}