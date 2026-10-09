package model;

import java.util.Arrays;
import java.util.List;

public class DeluxeRoom extends Room {
    public DeluxeRoom(int number, int floor, int capacity) { super(number, floor, capacity); }

    @Override public RoomType getType() { return RoomType.DELUXE; }
    @Override public double getPricePerNight() { return 4500.0; }
    @Override public List<String> getAmenities() {
        return Arrays.asList("King Bed", "City View", "Balcony", "Mini Bar", "Free Wi-Fi", "Breakfast");
    }
}
