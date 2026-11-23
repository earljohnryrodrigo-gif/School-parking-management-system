package model;

public enum UserType {
    STUDENT("Student"),
    SCHOOL_PERSONNEL("School Personnel"),
    VISITOR("Visitor"),
    PARKING_ADMINISTRATOR("Parking Administrator");

    private final String label;

    UserType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}