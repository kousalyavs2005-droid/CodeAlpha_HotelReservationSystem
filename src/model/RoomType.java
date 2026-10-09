package model;

public enum RoomType {
    STANDARD("Standard", "#5B7C99"),
    DELUXE("Deluxe", "#8E6BBF"),
    SUITE("Suite", "#C9A24B");

    private final String label;
    private final String colorHex;

    RoomType(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() { return label; }
    public String getColorHex() { return colorHex; }

    @Override public String toString() { return label; }
}
