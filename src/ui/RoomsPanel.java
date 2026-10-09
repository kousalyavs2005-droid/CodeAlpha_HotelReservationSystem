package ui;

import exception.RoomNotAvailableException;
import model.Booking;
import model.Room;
import model.RoomType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Room inventory: categories, rates, live occupancy, add / remove rooms. */
public class RoomsPanel extends JPanel implements Refreshable {
    private static final String[] COLS = {"Room", "Category", "Floor", "Sleeps", "Rate / night", "Amenities", "Today"};
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public RoomsPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);
        JButton add = Theme.gold("Add Room");
        JButton remove = Theme.danger("Remove Room");
        add.addActionListener(e -> addRoom());
        remove.addActionListener(e -> removeRoom());
        bar.add(add);
        bar.add(remove);
        add(bar, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {70, 100, 60, 60, 110, 420, 160};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        add(Theme.scroll(table), BorderLayout.CENTER);
    }

    @Override public void refresh() {
        model.setRowCount(0);
        for (Room r : AppContext.ROOMS.all()) {
            Booking occ = AppContext.ROOMS.currentOccupant(r.getNumber());
            model.addRow(new Object[]{r.getNumber(), r.getType().getLabel(), r.getFloor(), r.getCapacity(),
                    Theme.money(r.getPricePerNight()), r.getAmenitiesText(),
                    occ == null ? "Available" : "Occupied - " + occ.getGuest().getName()});
        }
    }

    private void addRoom() {
        JTextField number = Theme.field();
        JTextField floor = Theme.field();
        JTextField cap = Theme.field();
        JComboBox<Object> type = Theme.combo((Object[]) RoomType.values());
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.add(new JLabel("Room number")); p.add(number);
        p.add(new JLabel("Category")); p.add(type);
        p.add(new JLabel("Floor")); p.add(floor);
        p.add(new JLabel("Capacity (guests)")); p.add(cap);
        if (JOptionPane.showConfirmDialog(this, p, "Add room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            AppContext.ROOMS.addRoom((RoomType) type.getSelectedItem(), Integer.parseInt(number.getText().trim()),
                    Integer.parseInt(floor.getText().trim()), Integer.parseInt(cap.getText().trim()));
            refresh();
        } catch (NumberFormatException ex) {
            Theme.toast(this, "Room number, floor and capacity must be whole numbers.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException | RoomNotAvailableException ex) {
            Theme.toast(this, ex.getMessage(), "Cannot add room", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeRoom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.toast(this, "Select a room to remove.", "No room selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int number = (Integer) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Remove room " + number + " from the inventory?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            AppContext.ROOMS.removeRoom(number);
            refresh();
        } catch (RoomNotAvailableException ex) {
            Theme.toast(this, ex.getMessage(), "Cannot remove room", JOptionPane.WARNING_MESSAGE);
        }
    }
}
