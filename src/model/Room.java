package model;

import java.util.List;

/** Abstract base class: every room category supplies its own price and amenities (polymorphism). */
public abstract class Room {
    private final int number;
    private final int floor;
    private final int capacity;

    protected Room(int number, int floor, int capacity) {
        if (number <= 0) throw new IllegalArgumentException("Room number must be positive");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.number = number;
        this.floor = floor;
        this.capacity = capacity;
    }

    public abstract RoomType getType();
    public abstract double getPricePerNight();
    public abstract List<String> getAmenities();

    public int getNumber() { return number; }
    public int getFloor() { return floor; }
    public int getCapacity() { return capacity; }

    public String getAmenitiesText() { return String.join(" \u2022 ", getAmenities()); }

    /** Factory used when loading rooms from storage or adding new ones. */
    public static Room create(RoomType type, int number, int floor, int capacity) {
        switch (type) {
            case DELUXE: return new DeluxeRoom(number, floor, capacity);
            case SUITE:  return new SuiteRoom(number, floor, capacity);
            default:     return new StandardRoom(number, floor, capacity);
        }
    }

    @Override public String toString() { return "Room " + number + " (" + getType() + ")"; }
}
