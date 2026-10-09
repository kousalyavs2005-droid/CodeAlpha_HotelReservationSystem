package model;

public enum PaymentMethod {
    CARD("Credit / Debit Card"),
    UPI("UPI"),
    CASH("Pay at Hotel (Cash)");

    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
