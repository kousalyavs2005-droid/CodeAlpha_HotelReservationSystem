package util;

import model.Booking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Writes a booking receipt as a plain-text file (File I/O). */
public final class ReceiptExporter {
    private ReceiptExporter() { }

    public static Path export(Booking b) throws IOException {
        Path dir = Paths.get("receipts");
        Files.createDirectories(dir);
        Path file = dir.resolve(b.getId() + ".txt");
        Files.write(file, receiptText(b).getBytes(StandardCharsets.UTF_8));
        return file.toAbsolutePath();
    }

    public static String receiptText(Booking b) {
        String line = "=".repeat(44);
        StringBuilder sb = new StringBuilder();
        sb.append(line).append('\n');
        sb.append("        GRAND AURORA HOTEL & RESORT\n");
        sb.append("             BOOKING RECEIPT\n");
        sb.append(line).append('\n');
        row(sb, "Booking ID", b.getId());
        row(sb, "Status", b.effectiveStatus().getLabel());
        row(sb, "Booked on", b.getCreatedAt().format(DateUtil.DATE_TIME));
        sb.append('\n');
        row(sb, "Guest", b.getGuest().getName());
        row(sb, "Phone", b.getGuest().getPhone());
        row(sb, "Email", b.getGuest().getEmail());
        sb.append('\n');
        row(sb, "Room", b.getRoomNumber() + " (" + b.getRoomType().getLabel() + ")");
        row(sb, "Check-in", b.getCheckIn().format(DateUtil.DISPLAY));
        row(sb, "Check-out", b.getCheckOut().format(DateUtil.DISPLAY));
        row(sb, "Nights", String.valueOf(b.getNights()));
        row(sb, "Guests", String.valueOf(b.getGuestCount()));
        sb.append('\n');
        row(sb, "Subtotal", money(b.getSubtotal()));
        row(sb, "GST (12%)", money(b.getTax()));
        row(sb, "TOTAL", money(b.getTotal()));
        sb.append('\n');
        row(sb, "Payment method", b.getPayment().getMethod().getLabel());
        row(sb, "Payment status", b.getPayment().getStatus().getLabel());
        row(sb, "Transaction ID", b.getPayment().getTransactionId());
        if (b.getRefundAmount() > 0) row(sb, "Refund issued", money(b.getRefundAmount()));
        sb.append(line).append('\n');
        sb.append("      Thank you for choosing Grand Aurora\n");
        return sb.toString();
    }

    private static void row(StringBuilder sb, String k, String v) {
        sb.append(String.format("%-18s %s%n", k + ":", v));
    }

    public static String money(double v) {
        return String.format("Rs. %,.2f", v);
    }
}
