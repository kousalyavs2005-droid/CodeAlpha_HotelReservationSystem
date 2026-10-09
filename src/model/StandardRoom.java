package model;

import java.util.Arrays;
import java.util.List;

public class StandardRoom extends Room {
    public StandardRoom(int number, int floor, int capacity) { super(number, floor, capacity); }

    @Override public RoomType getType() { return RoomType.STANDARD; }
    @Override public double getPricePerNight() { return 2500.0; }
    @Override public List<String> getAmenities() {
        return Arrays.asList("Free Wi-Fi", "Air Conditioning", "Smart TV", "Work Desk");
    }
}
