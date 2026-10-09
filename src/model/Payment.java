package model;

import java.time.LocalDateTime;

public class Payment {
    private final String transactionId;
    private final PaymentMethod method;
    private final double amount;
    private PaymentStatus status;
    private final LocalDateTime time;

    public Payment(String transactionId, PaymentMethod method, double amount,
                   PaymentStatus status, LocalDateTime time) {
        this.transactionId = transactionId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.time = time;
    }

    public String getTransactionId() { return transactionId; }
    public PaymentMethod getMethod() { return method; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getTime() { return time; }
}
