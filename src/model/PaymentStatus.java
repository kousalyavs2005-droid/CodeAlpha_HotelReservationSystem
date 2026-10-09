package model;

public enum PaymentStatus {
    PAID("Paid"),
    PENDING("Pay at hotel"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially refunded"),
    VOID("Void");

    private final String label;
    PaymentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
