package ui;

import exception.InvalidDateException;
import model.Room;
import model.RoomType;
import service.BookingService;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Search available rooms by date / category / guests / budget and start a booking. */
public class SearchPanel extends JPanel implements Refreshable {
    private final JSpinner inSpinner = dateSpinner(LocalDate.now());
    private final JSpinner outSpinner = dateSpinner(LocalDate.now().plusDays(1));
    private final JSpinner guestSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50000, 500));
    private final JComboBox<Object> typeBox;
    private final JPanel results = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JLabel summary = Theme.label(" ", Theme.BOLD, Theme.MUTED);

    public SearchPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        Object[] types = new Object[RoomType.values().length + 1];
        types[0] = "All categories";
        System.arraycopy(RoomType.values(), 0, types, 1, RoomType.values().length);
        typeBox = Theme.combo(types);
        for (JSpinner s : new JSpinner[]{inSpinner, outSpinner, guestSpinner, priceSpinner}) Theme.styleInput(s);

        add(filterCard(), BorderLayout.NORTH);

        results.setOpaque(false);
        JPanel holder = new WidthTrackingPanel();
        holder.setOpaque(false);
        holder.add(summary, BorderLayout.NORTH);
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        grid.add(results, BorderLayout.NORTH);
        holder.add(grid, BorderLayout.CENTER);
        JScrollPane sp = new JScrollPane(holder);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        add(sp, BorderLayout.CENTER);
    }

    private static JSpinner dateSpinner(LocalDate d) {
        JSpinner s = new JSpinner(new SpinnerDateModel(DateUtil.toDate(d), null, null, Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd MMM yyyy"));
        return s;
    }

    private JPanel filterCard() {
        Theme.RoundedPanel card = new Theme.RoundedPanel(16, Theme.CARD, true);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        String[] labels = {"Check-in", "Check-out", "Category", "Guests", "Max price / night (0 = any)"};
        JComponent[] comps = {inSpinner, outSpinner, typeBox, guestSpinner, priceSpinner};
        for (int i = 0; i < labels.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 4, 12);
            card.add(Theme.label(labels[i], Theme.SMALL, Theme.MUTED), c);
            c.gridy = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 0, 12);
            card.add(comps[i], c);
        }
        JButton search = Theme.gold("Search Rooms");
        search.addActionListener(e -> runSearch());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = labels.length; c.gridy = 1; c.anchor = GridBagConstraints.SOUTH;
        card.add(search, c);
        return card;
    }

    @Override public void refresh() { runSearch(); }

    private void runSearch() {
        LocalDate in = DateUtil.fromDate((Date) inSpinner.getValue());
        LocalDate out = DateUtil.fromDate((Date) outSpinner.getValue());
        try {
            BookingService.validateDates(in, out);
        } catch (InvalidDateException ex) {
            results.removeAll();
            summary.setForeground(Theme.DANGER);
            summary.setText(ex.getMessage());
            results.revalidate();
            results.repaint();
            return;
        }
        Object sel = typeBox.getSelectedItem();
        RoomType type = sel instanceof RoomType ? (RoomType) sel : null;
        int guests = (Integer) guestSpinner.getValue();
        double max = ((Number) priceSpinner.getValue()).doubleValue();

        List<Room> found = AppContext.ROOMS.search(in, out, type, guests, max);
        results.removeAll();
        long nights = DateUtil.nights(in, out);
        summary.setForeground(Theme.MUTED);
        summary.setText(found.isEmpty()
                ? "No rooms match these dates and filters. Try different dates or relax the filters."
                : found.size() + " room(s) available  \u00B7  " + nights + " night(s)  \u00B7  "
                  + in.format(DateUtil.DISPLAY) + " \u2192 " + out.format(DateUtil.DISPLAY));
        for (Room r : found) results.add(roomCard(r, in, out, guests, nights));
        results.revalidate();
        results.repaint();
    }

    private JPanel roomCard(Room r, LocalDate in, LocalDate out, int guests, long nights) {
        Theme.RoundedPanel card = new Theme.RoundedPanel(16, Theme.CARD, true);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 20, 18));
        card.setPreferredSize(new Dimension(10, 215));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Theme.label("Room " + r.getNumber(), Theme.HEADING, Theme.NAVY), BorderLayout.WEST);
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        badgeWrap.setOpaque(false);
        badgeWrap.add(new Theme.Badge(r.getType().getLabel().toUpperCase(), Theme.hex(r.getType().getColorHex())));
        top.add(badgeWrap, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JLabel info = Theme.label("<html>Floor " + r.getFloor() + "  \u00B7  Sleeps " + r.getCapacity()
                + "<br><br><span style='color:#6B7280'>" + r.getAmenitiesText() + "</span></html>", Theme.BODY, Theme.TEXT);
        info.setVerticalAlignment(SwingConstants.TOP);
        card.add(info, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JPanel price = new JPanel();
        price.setOpaque(false);
        price.setLayout(new BoxLayout(price, BoxLayout.Y_AXIS));
        JLabel p1 = Theme.label(Theme.money(r.getPricePerNight()) + " / night", Theme.font(Font.BOLD, 16), Theme.GOLD_DARK);
        JLabel p2 = Theme.label(Theme.money(r.getPricePerNight() * nights) + " \u00B7 " + nights + (nights == 1 ? " night" : " nights"), Theme.SMALL, Theme.MUTED);
        p1.setAlignmentX(LEFT_ALIGNMENT);
        p2.setAlignmentX(LEFT_ALIGNMENT);
        price.add(p1);
        price.add(p2);
        bottom.add(price, BorderLayout.CENTER);
        JButton book = Theme.navy("Book Now");
        book.addActionListener(e -> {
            new BookingDialog(SwingUtilities.getWindowAncestor(this), r, in, out, guests).showDialog();
            runSearch();
        });
        bottom.add(book, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    /** Panel that always matches the scroll pane's width, so the card grid reflows instead of scrolling sideways. */
    private static class WidthTrackingPanel extends JPanel implements Scrollable {
        WidthTrackingPanel() { super(new BorderLayout()); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
