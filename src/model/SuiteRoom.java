package model;

import java.util.Arrays;
import java.util.List;

public class SuiteRoom extends Room {
    public SuiteRoom(int number, int floor, int capacity) { super(number, floor, capacity); }

    @Override public RoomType getType() { return RoomType.SUITE; }
    @Override public double getPricePerNight() { return 9000.0; }
    @Override public List<String> getAmenities() {
        return Arrays.asList("Living Area", "Jacuzzi", "Butler Service", "Lounge Access", "Mini Bar", "Breakfast");
    }
}
