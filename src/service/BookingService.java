package service;

import dao.BookingDAO;
import dao.RoomDAO;
import exception.InvalidDateException;
import exception.PaymentException;
import exception.RoomNotAvailableException;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingService {
    public static final double GST_RATE = 0.12;

    private final BookingDAO bookingDAO;
    private final RoomDAO roomDAO;
    private final RoomService roomService;
    private final PaymentService paymentService;

    public BookingService(BookingDAO bookingDAO, RoomDAO roomDAO, RoomService roomService, PaymentService paymentService) {
        this.bookingDAO = bookingDAO;
        this.roomDAO = roomDAO;
        this.roomService = roomService;
        this.paymentService = paymentService;
    }

    /** Price breakdown for a stay. */
    public static class Quote {
        public final long nights;
        public final double rate;
        public final double subtotal;
        public final double tax;
        public final double total;

        Quote(long nights, double rate) {
            this.nights = nights;
            this.rate = rate;
            this.subtotal = nights * rate;
            this.tax = Math.round(subtotal * GST_RATE * 100.0) / 100.0;
            this.total = subtotal + tax;
        }
    }

    public static void validateDates(LocalDate in, LocalDate out) throws InvalidDateException {
        if (in == null || out == null) throw new InvalidDateException("Please select both dates.");
        if (in.isBefore(LocalDate.now())) throw new InvalidDateException("Check-in cannot be in the past.");
        if (!out.isAfter(in)) throw new InvalidDateException("Check-out must be after check-in.");
    }

    public Quote quote(Room room, LocalDate in, LocalDate out) {
        return new Quote(util.DateUtil.nights(in, out), room.getPricePerNight());
    }

    /** Validates, checks availability, takes (simulated) payment and stores the booking. */
    public synchronized Booking book(Guest guest, int roomNumber, LocalDate in, LocalDate out,
                                     int guestCount, PaymentRequest payReq)
            throws InvalidDateException, RoomNotAvailableException, PaymentException {
        validateDates(in, out);
        Room room = roomDAO.findByNumber(roomNumber)
                .orElseThrow(() -> new RoomNotAvailableException("Room " + roomNumber + " does not exist."));
        if (guestCount > room.getCapacity())
            throw new RoomNotAvailableException("Room " + roomNumber + " sleeps at most " + room.getCapacity() + " guests.");
        if (!roomService.isAvailable(roomNumber, in, out))
            throw new RoomNotAvailableException("Room " + roomNumber + " was just booked for these dates.");

        Quote q = quote(room, in, out);
        Payment payment = paymentService.process(payReq, q.total);
        Booking b = new Booking(bookingDAO.nextId(), guest, roomNumber, room.getType(), in, out, guestCount,
                q.subtotal, q.tax, q.total, BookingStatus.CONFIRMED, payment, 0.0, LocalDateTime.now());
        bookingDAO.add(b);
        return b;
    }

    /** Refund policy: 100% if 2+ days before check-in, 50% if 1 day before, otherwise none. */
    public double refundFor(Booking b) {
        if (b.getPayment().getStatus() != PaymentStatus.PAID) return 0.0;
        long daysToCheckIn = util.DateUtil.nights(LocalDate.now(), b.getCheckIn());
        if (daysToCheckIn >= 2) return b.getTotal();
        if (daysToCheckIn == 1) return Math.round(b.getTotal() * 50.0) / 100.0;
        return 0.0;
    }

    public synchronized Booking cancel(String bookingId) throws RoomNotAvailableException {
        Booking b = bookingDAO.findById(bookingId)
                .orElseThrow(() -> new RoomNotAvailableException("Booking " + bookingId + " not found."));
        if (b.getStatus() == BookingStatus.CANCELLED)
            throw new RoomNotAvailableException("This booking is already cancelled.");
        if (b.getCheckIn().isBefore(LocalDate.now()))
            throw new RoomNotAvailableException("This stay has already started or finished, so it cannot be cancelled.");

        double refund = refundFor(b);
        b.setStatus(BookingStatus.CANCELLED);
        b.setRefundAmount(refund);
        Payment p = b.getPayment();
        if (p.getStatus() == PaymentStatus.PAID)
            p.setStatus(refund >= b.getTotal() ? PaymentStatus.REFUNDED : refund > 0 ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.PAID);
        else if (p.getStatus() == PaymentStatus.PENDING)
            p.setStatus(PaymentStatus.VOID);
        bookingDAO.update(b);
        return b;
    }

    public List<Booking> all() {
        List<Booking> list = bookingDAO.findAll();
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return list;
    }

    public List<Booking> search(String text, BookingStatus status) {
        String t = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        List<Booking> out = new ArrayList<>();
        for (Booking b : all()) {
            if (status != null && b.effectiveStatus() != status) continue;
            if (!t.isEmpty() && !(b.getId().toLowerCase(Locale.ROOT).contains(t)
                    || b.getGuest().getName().toLowerCase(Locale.ROOT).contains(t)
                    || String.valueOf(b.getRoomNumber()).contains(t)
                    || b.getGuest().getPhone().contains(t))) continue;
            out.add(b);
        }
        return out;
    }

    // ---- dashboard figures ----
    public int activeBookings() {
        int n = 0;
        for (Booking b : bookingDAO.findAll())
            if (b.effectiveStatus() == BookingStatus.CONFIRMED) n++;
        return n;
    }

    public double revenueCollected() {
        double sum = 0;
        for (Booking b : bookingDAO.findAll()) {
            if (b.getPayment().getStatus() == PaymentStatus.PAID) sum += b.getTotal();
            else if (b.getPayment().getStatus() == PaymentStatus.PARTIALLY_REFUNDED) sum += b.getTotal() - b.getRefundAmount();
        }
        return sum;
    }
}
