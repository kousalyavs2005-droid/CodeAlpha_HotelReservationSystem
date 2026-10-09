package model;

import util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private final String id;
    private final Guest guest;
    private final int roomNumber;
    private final RoomType roomType;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int guestCount;
    private final double subtotal;
    private final double tax;
    private final double total;
    private BookingStatus status;
    private final Payment payment;
    private double refundAmount;
    private final LocalDateTime createdAt;

    public Booking(String id, Guest guest, int roomNumber, RoomType roomType, LocalDate checkIn,
                   LocalDate checkOut, int guestCount, double subtotal, double tax, double total,
                   BookingStatus status, Payment payment, double refundAmount, LocalDateTime createdAt) {
        this.id = id;
        this.guest = guest;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.status = status;
        this.payment = payment;
        this.refundAmount = refundAmount;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public Guest getGuest() { return guest; }
    public int getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getGuestCount() { return guestCount; }
    public double getSubtotal() { return subtotal; }
    public double getTax() { return tax; }
    public double getTotal() { return total; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Payment getPayment() { return payment; }
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public long getNights() { return DateUtil.nights(checkIn, checkOut); }

    /** A confirmed booking whose check-out date has passed is shown as Completed. */
    public BookingStatus effectiveStatus() {
        if (status == BookingStatus.CONFIRMED && !checkOut.isAfter(LocalDate.now())) {
            return BookingStatus.COMPLETED;
        }
        return status;
    }

    /** True when this booking currently blocks the room. */
    public boolean blocksRoom() { return status == BookingStatus.CONFIRMED; }
}
