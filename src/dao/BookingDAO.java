package dao;

import model.*;
import util.FileStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores bookings (with guest + payment details) in data/bookings.txt. */
public class BookingDAO {
    private static final String FILE = "bookings.txt";
    private final List<Booking> bookings = new ArrayList<>();

    public BookingDAO() {
        for (String line : FileStore.readLines(FILE)) {
            if (line.trim().isEmpty()) continue;
            bookings.add(fromLine(line));
        }
    }

    public synchronized List<Booking> findAll() { return new ArrayList<>(bookings); }

    public synchronized Optional<Booking> findById(String id) {
        return bookings.stream().filter(b -> b.getId().equalsIgnoreCase(id)).findFirst();
    }

    public synchronized String nextId() {
        int max = 1000;
        for (Booking b : bookings) {
            try { max = Math.max(max, Integer.parseInt(b.getId().substring(2))); }
            catch (NumberFormatException ignored) { }
        }
        return "BK" + (max + 1);
    }

    public synchronized void add(Booking b) {
        bookings.add(b);
        persist();
    }

    /** Call after mutating a booking that is already in the list. */
    public synchronized void update(Booking b) { persist(); }

    private void persist() {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) lines.add(toLine(b));
        FileStore.writeLines(FILE, lines);
    }

    private String toLine(Booking b) {
        Payment p = b.getPayment();
        return FileStore.join(
                b.getId(), b.getGuest().getName(), b.getGuest().getPhone(), b.getGuest().getEmail(),
                String.valueOf(b.getRoomNumber()), b.getRoomType().name(),
                b.getCheckIn().toString(), b.getCheckOut().toString(), String.valueOf(b.getGuestCount()),
                String.valueOf(b.getSubtotal()), String.valueOf(b.getTax()), String.valueOf(b.getTotal()),
                b.getStatus().name(), p.getMethod().name(), p.getTransactionId(), p.getStatus().name(),
                String.valueOf(p.getAmount()), p.getTime().toString(),
                String.valueOf(b.getRefundAmount()), b.getCreatedAt().toString());
    }

    private Booking fromLine(String line) {
        String[] f = FileStore.split(line);
        Guest g = new Guest(f[1], f[2], f[3]);
        Payment p = new Payment(f[14], PaymentMethod.valueOf(f[13]), Double.parseDouble(f[16]),
                PaymentStatus.valueOf(f[15]), LocalDateTime.parse(f[17]));
        return new Booking(f[0], g, Integer.parseInt(f[4]), RoomType.valueOf(f[5]),
                LocalDate.parse(f[6]), LocalDate.parse(f[7]), Integer.parseInt(f[8]),
                Double.parseDouble(f[9]), Double.parseDouble(f[10]), Double.parseDouble(f[11]),
                BookingStatus.valueOf(f[12]), p, Double.parseDouble(f[18]), LocalDateTime.parse(f[19]));
    }
}
