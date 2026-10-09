package service;

import dao.BookingDAO;
import dao.RoomDAO;
import exception.RoomNotAvailableException;
import model.Booking;
import model.Room;
import model.RoomType;
import util.DateUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    private final RoomDAO roomDAO;
    private final BookingDAO bookingDAO;

    public RoomService(RoomDAO roomDAO, BookingDAO bookingDAO) {
        this.roomDAO = roomDAO;
        this.bookingDAO = bookingDAO;
    }

    public List<Room> all() { return roomDAO.findAll(); }

    public boolean isAvailable(int roomNumber, LocalDate in, LocalDate out) {
        for (Booking b : bookingDAO.findAll()) {
            if (b.getRoomNumber() == roomNumber && b.blocksRoom()
                    && DateUtil.overlaps(in, out, b.getCheckIn(), b.getCheckOut())) {
                return false;
            }
        }
        return true;
    }

    /** Search by dates plus optional category, guest count and max price (maxPrice <= 0 means any). */
    public List<Room> search(LocalDate in, LocalDate out, RoomType type, int guests, double maxPrice) {
        List<Room> result = new ArrayList<>();
        for (Room r : roomDAO.findAll()) {
            if (type != null && r.getType() != type) continue;
            if (r.getCapacity() < guests) continue;
            if (maxPrice > 0 && r.getPricePerNight() > maxPrice) continue;
            if (!isAvailable(r.getNumber(), in, out)) continue;
            result.add(r);
        }
        return result;
    }

    /** Status of a room for today: occupied by whom, or null if free. */
    public Booking currentOccupant(int roomNumber) {
        LocalDate today = LocalDate.now();
        for (Booking b : bookingDAO.findAll()) {
            if (b.getRoomNumber() == roomNumber && b.blocksRoom()
                    && !today.isBefore(b.getCheckIn()) && today.isBefore(b.getCheckOut())) {
                return b;
            }
        }
        return null;
    }

    public void addRoom(RoomType type, int number, int floor, int capacity) throws RoomNotAvailableException {
        if (roomDAO.findByNumber(number).isPresent())
            throw new RoomNotAvailableException("Room " + number + " already exists.");
        roomDAO.add(Room.create(type, number, floor, capacity));
    }

    public void removeRoom(int number) throws RoomNotAvailableException {
        for (Booking b : bookingDAO.findAll()) {
            if (b.getRoomNumber() == number && b.blocksRoom() && b.getCheckOut().isAfter(LocalDate.now()))
                throw new RoomNotAvailableException("Room " + number + " has upcoming reservations and cannot be removed.");
        }
        roomDAO.remove(number);
    }
}
