package ui;

import model.Booking;
import model.BookingStatus;
import model.PaymentStatus;
import util.DateUtil;
import util.ReceiptExporter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;

/** Manage reservations: filter, view details, cancel, export receipt. */
public class BookingsPanel extends JPanel implements Refreshable {
    private static final String[] COLS = {"Booking ID", "Guest", "Room", "Category", "Check-in", "Check-out", "Total", "Status", "Payment"};

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = Theme.field();
    private final JComboBox<Object> statusBox;

    public BookingsPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        Object[] opts = new Object[BookingStatus.values().length + 1];
        opts[0] = "All statuses";
        System.arraycopy(BookingStatus.values(), 0, opts, 1, BookingStatus.values().length);
        statusBox = Theme.combo(opts);

        JPanel bar = new JPanel(new GridBagLayout());
        bar.setOpaque(false);
        search.setToolTipText("Search by booking ID, guest name, phone or room number");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 0, 10);
        bar.add(search, c);
        c.gridx = 1; c.weightx = 0;
        statusBox.setPreferredSize(new Dimension(170, 38));
        bar.add(statusBox, c);
        JButton view = Theme.navy("View Details");
        JButton cancel = Theme.danger("Cancel Booking");
        JButton export = Theme.ghost("Save Receipt");
        c.gridx = 2; bar.add(view, c);
        c.gridx = 3; bar.add(export, c);
        c.gridx = 4; c.insets = new Insets(0, 0, 0, 0); bar.add(cancel, c);
        add(bar, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {95, 170, 70, 100, 110, 110, 110, 105, 150};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        add(Theme.scroll(table), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        statusBox.addActionListener(e -> refresh());
        view.addActionListener(e -> showDetails());
        export.addActionListener(e -> exportReceipt());
        cancel.addActionListener(e -> cancelSelected());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) showDetails(); }
        });
    }

    @Override public void refresh() {
        Object sel = statusBox.getSelectedItem();
        BookingStatus st = sel instanceof BookingStatus ? (BookingStatus) sel : null;
        List<Booking> list = AppContext.BOOKINGS.search(search.getText(), st);
        model.setRowCount(0);
        for (Booking b : list) {
            model.addRow(new Object[]{b.getId(), b.getGuest().getName(), b.getRoomNumber(), b.getRoomType().getLabel(),
                    b.getCheckIn().format(DateUtil.DISPLAY), b.getCheckOut().format(DateUtil.DISPLAY),
                    Theme.moneyExact(b.getTotal()), b.effectiveStatus().getLabel(), b.getPayment().getStatus().getLabel()});
        }
    }

    private Booking selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Theme.toast(this, "Please select a booking from the table first.", "No booking selected", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
        return AppContext.BOOKING_DAO.findById(id).orElse(null);
    }

    private void showDetails() {
        Booking b = selected();
        if (b != null) new BookingDetailsDialog(SwingUtilities.getWindowAncestor(this), b, "Booking Details").setVisible(true);
    }

    private void exportReceipt() {
        Booking b = selected();
        if (b == null) return;
        try {
            Path p = ReceiptExporter.export(b);
            Theme.toast(this, "Receipt saved to:\n" + p, "Receipt saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Theme.toast(this, "Could not save receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelected() {
        Booking b = selected();
        if (b == null) return;
        double refund = AppContext.BOOKINGS.refundFor(b);
        String msg = "Cancel booking " + b.getId() + " for " + b.getGuest().getName() + "?\n\n";
        if (b.getPayment().getStatus() == PaymentStatus.PAID)
            msg += "Refund: " + Theme.moneyExact(refund) + " of " + Theme.moneyExact(b.getTotal())
                    + "\n(100% if 2+ days before check-in, 50% if 1 day before, otherwise none)";
        else msg += "No payment was taken, so there is nothing to refund.";
        int ans = JOptionPane.showConfirmDialog(this, msg, "Confirm cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        try {
            AppContext.BOOKINGS.cancel(b.getId());
            refresh();
            Theme.toast(this, "Booking " + b.getId() + " has been cancelled and the room is available again.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Theme.toast(this, ex.getMessage(), "Cannot cancel", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Coloured status text. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            String s = String.valueOf(v);
            l.setFont(Theme.BOLD);
            l.setForeground(s.equals("Confirmed") ? Theme.SUCCESS : s.equals("Cancelled") ? Theme.DANGER : Theme.MUTED);
            if (!sel) l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFBFAF7));
            return l;
        }
    }
}
