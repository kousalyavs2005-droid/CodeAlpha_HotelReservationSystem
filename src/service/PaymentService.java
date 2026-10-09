package service;

import exception.PaymentException;
import model.Payment;
import model.PaymentMethod;
import model.PaymentStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** Simulated payment gateway. Test card: 4111 1111 1111 1111, any future expiry, any 3-digit CVV. */
public class PaymentService {
    private final Random random = new Random();

    public Payment process(PaymentRequest req, double amount) throws PaymentException {
        switch (req.method) {
            case CARD: validateCard(req); break;
            case UPI:  validateUpi(req); break;
            default:   break;
        }
        simulateNetworkDelay();
        PaymentStatus status = req.method == PaymentMethod.CASH ? PaymentStatus.PENDING : PaymentStatus.PAID;
        return new Payment(newTransactionId(), req.method, amount, status, LocalDateTime.now());
    }

    private void validateCard(PaymentRequest r) throws PaymentException {
        String number = r.cardNumber == null ? "" : r.cardNumber.replaceAll("[\\s-]", "");
        if (r.cardHolder == null || r.cardHolder.trim().length() < 3)
            throw new PaymentException("Enter the name on the card.");
        if (!number.matches("\\d{16}") || !luhn(number))
            throw new PaymentException("Card number is invalid. Use a 16-digit number (test card: 4111 1111 1111 1111).");
        if (r.cvv == null || !r.cvv.matches("\\d{3}"))
            throw new PaymentException("CVV must be 3 digits.");
        try {
            YearMonth exp = YearMonth.parse(r.expiry.trim(), DateTimeFormatter.ofPattern("MM/yy"));
            if (exp.isBefore(YearMonth.now())) throw new PaymentException("This card has expired.");
        } catch (java.time.format.DateTimeParseException | NullPointerException e) {
            throw new PaymentException("Expiry must be in MM/YY format.");
        }
    }

    private void validateUpi(PaymentRequest r) throws PaymentException {
        if (r.upiId == null || !r.upiId.trim().matches("[\\w.\\-]{2,}@[a-zA-Z]{2,}"))
            throw new PaymentException("Enter a valid UPI ID, e.g. name@bank.");
    }

    private boolean luhn(String digits) {
        int sum = 0;
        boolean alt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alt) { n *= 2; if (n > 9) n -= 9; }
            sum += n;
            alt = !alt;
        }
        return sum % 10 == 0;
    }

    private void simulateNetworkDelay() {
        try { Thread.sleep(1200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String newTransactionId() {
        return "TXN" + System.currentTimeMillis() % 100000000L + (100 + random.nextInt(900));
    }
}
