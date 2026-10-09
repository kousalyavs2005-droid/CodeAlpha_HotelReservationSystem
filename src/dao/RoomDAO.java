package dao;

import model.Room;
import model.RoomType;
import util.FileStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Stores rooms in data/rooms.txt (number|type|floor|capacity). Seeds a default inventory on first run. */
public class RoomDAO {
    private static final String FILE = "rooms.txt";
    private final List<Room> rooms = new ArrayList<>();

    public RoomDAO() {
        if (!FileStore.exists(FILE)) {
            seed();
            persist();
        } else {
            load();
        }
    }

    private void seed() {
        for (int n = 101; n <= 108; n++) rooms.add(Room.create(RoomType.STANDARD, n, 1 + (n > 104 ? 1 : 0), n % 2 == 0 ? 2 : 3));
        for (int n = 201; n <= 206; n++) rooms.add(Room.create(RoomType.DELUXE, n, 2 + (n > 203 ? 1 : 0), n % 2 == 0 ? 3 : 2));
        for (int n = 301; n <= 304; n++) rooms.add(Room.create(RoomType.SUITE, n, 4, n % 2 == 0 ? 4 : 5));
    }

    private void load() {
        for (String line : FileStore.readLines(FILE)) {
            if (line.trim().isEmpty()) continue;
            String[] f = FileStore.split(line);
            rooms.add(Room.create(RoomType.valueOf(f[1]), Integer.parseInt(f[0]),
                    Integer.parseInt(f[2]), Integer.parseInt(f[3])));
        }
    }

    private void persist() {
        rooms.sort(Comparator.comparingInt(Room::getNumber));
        List<String> lines = new ArrayList<>();
        for (Room r : rooms) {
            lines.add(FileStore.join(String.valueOf(r.getNumber()), r.getType().name(),
                    String.valueOf(r.getFloor()), String.valueOf(r.getCapacity())));
        }
        FileStore.writeLines(FILE, lines);
    }

    public synchronized List<Room> findAll() { return new ArrayList<>(rooms); }

    public synchronized Optional<Room> findByNumber(int number) {
        return rooms.stream().filter(r -> r.getNumber() == number).findFirst();
    }

    public synchronized void add(Room room) {
        rooms.add(room);
        persist();
    }

    public synchronized void remove(int number) {
        rooms.removeIf(r -> r.getNumber() == number);
        persist();
    }
}
