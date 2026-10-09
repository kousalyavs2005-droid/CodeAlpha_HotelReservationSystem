package service;

import model.PaymentMethod;

/** Payment details entered by the guest (simulation only; nothing is stored except the method). */
public class PaymentRequest {
    public final PaymentMethod method;
    public final String cardNumber;
    public final String cardHolder;
    public final String expiry;   // MM/yy
    public final String cvv;
    public final String upiId;

    public PaymentRequest(PaymentMethod method, String cardNumber, String cardHolder,
                          String expiry, String cvv, String upiId) {
        this.method = method;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiry = expiry;
        this.cvv = cvv;
        this.upiId = upiId;
    }

    public static PaymentRequest cash() { return new PaymentRequest(PaymentMethod.CASH, null, null, null, null, null); }
}
