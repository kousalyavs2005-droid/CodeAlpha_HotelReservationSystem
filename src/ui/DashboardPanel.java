package ui;

import model.Booking;
import model.Room;
import model.RoomType;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Overview: key figures, occupancy by category, recent bookings. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final JLabel totalRooms = bigNumber();
    private final JLabel freeTonight = bigNumber();
    private final JLabel active = bigNumber();
    private final JLabel revenue = bigNumber();
    private final JPanel occupancy = new JPanel();
    private final DefaultTableModel recentModel = new DefaultTableModel(
            new String[]{"ID", "Guest", "Room", "Check-in", "Total"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("Total rooms", totalRooms));
        stats.add(statCard("Free tonight", freeTonight));
        stats.add(statCard("Active bookings", active));
        stats.add(statCard("Revenue collected", revenue));
        add(stats, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);

        Theme.RoundedPanel occCard = new Theme.RoundedPanel(16, Theme.CARD, true);
        occCard.setLayout(new BorderLayout(0, 12));
        occCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 22, 20));
        occCard.add(Theme.label("Occupancy tonight", Theme.HEADING, Theme.NAVY), BorderLayout.NORTH);
        occupancy.setOpaque(false);
        occupancy.setLayout(new BoxLayout(occupancy, BoxLayout.Y_AXIS));
        occCard.add(occupancy, BorderLayout.CENTER);
        lower.add(occCard);

        Theme.RoundedPanel recentCard = new Theme.RoundedPanel(16, Theme.CARD, true);
        recentCard.setLayout(new BorderLayout(0, 12));
        recentCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 22, 20));
        recentCard.add(Theme.label("Recent bookings", Theme.HEADING, Theme.NAVY), BorderLayout.NORTH);
        JTable t = new JTable(recentModel);
        Theme.styleTable(t);
        recentCard.add(Theme.scroll(t), BorderLayout.CENTER);
        lower.add(recentCard);
        add(lower, BorderLayout.CENTER);
    }

    private static JLabel bigNumber() { return Theme.label("0", Theme.font(Font.BOLD, 30), Theme.NAVY); }

    private JPanel statCard(String title, JLabel value) {
        Theme.RoundedPanel c = new Theme.RoundedPanel(16, Theme.CARD, true);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        JLabel t = Theme.label(title.toUpperCase(), Theme.font(Font.BOLD, 11), Theme.GOLD_DARK);
        t.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        c.add(t);
        c.add(Box.createVerticalStrut(6));
        c.add(value);
        return c;
    }

    @Override public void refresh() {
        List<Room> rooms = AppContext.ROOMS.all();
        int occupiedTotal = 0;
        occupancy.removeAll();
        for (RoomType type : RoomType.values()) {
            int total = 0, occ = 0;
            for (Room r : rooms) {
                if (r.getType() != type) continue;
                total++;
                if (AppContext.ROOMS.currentOccupant(r.getNumber()) != null) occ++;
            }
            occupiedTotal += occ;
            occupancy.add(occupancyRow(type, occ, total));
            occupancy.add(Box.createVerticalStrut(14));
        }
        totalRooms.setText(String.valueOf(rooms.size()));
        freeTonight.setText(String.valueOf(rooms.size() - occupiedTotal));
        active.setText(String.valueOf(AppContext.BOOKINGS.activeBookings()));
        revenue.setText(Theme.money(AppContext.BOOKINGS.revenueCollected()));

        recentModel.setRowCount(0);
        List<Booking> all = AppContext.BOOKINGS.all();
        for (int i = 0; i < Math.min(8, all.size()); i++) {
            Booking b = all.get(i);
            recentModel.addRow(new Object[]{b.getId(), b.getGuest().getName(), b.getRoomNumber(),
                    b.getCheckIn().format(DateUtil.DISPLAY), Theme.money(b.getTotal())});
        }
        occupancy.revalidate();
        occupancy.repaint();
    }

    private JPanel occupancyRow(RoomType type, int occ, int total) {
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.label(type.getLabel(), Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        top.add(Theme.label(occ + " / " + total + " occupied", Theme.SMALL, Theme.MUTED), BorderLayout.EAST);
        row.add(top, BorderLayout.NORTH);
        final double frac = total == 0 ? 0 : (double) occ / total;
        final Color col = Theme.hex(type.getColorHex());
        JComponent bar = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEDE9DF));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(col);
                int w = (int) (getWidth() * frac);
                if (w > 0) g2.fillRoundRect(0, 0, Math.max(w, getHeight()), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        bar.setPreferredSize(new Dimension(10, 10));
        row.add(bar, BorderLayout.CENTER);
        return row;
    }
}
