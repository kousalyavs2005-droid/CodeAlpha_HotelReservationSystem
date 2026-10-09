package ui;

import dao.BookingDAO;
import dao.RoomDAO;
import service.BookingService;
import service.PaymentService;
import service.RoomService;

/** Wires the DAO and service layers together once for the whole application. */
public final class AppContext {
    public static final RoomDAO ROOM_DAO = new RoomDAO();
    public static final BookingDAO BOOKING_DAO = new BookingDAO();
    public static final PaymentService PAYMENTS = new PaymentService();
    public static final RoomService ROOMS = new RoomService(ROOM_DAO, BOOKING_DAO);
    public static final BookingService BOOKINGS = new BookingService(BOOKING_DAO, ROOM_DAO, ROOMS, PAYMENTS);

    private AppContext() { }
}
