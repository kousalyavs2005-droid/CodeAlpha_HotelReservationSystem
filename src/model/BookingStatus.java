package model;

public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String label;
    BookingStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
