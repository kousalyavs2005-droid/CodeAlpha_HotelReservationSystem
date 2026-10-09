package ui;

import model.Booking;
import util.DateUtil;
import util.ReceiptExporter;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

/** Receipt-style view of one booking. */
public class BookingDetailsDialog extends JDialog {

    public BookingDetailsDialog(Window owner, Booking b, String heading) {
        super(owner, "Booking " + b.getId(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);

        JPanel head = new JPanel();
        head.setBackground(Theme.NAVY);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
        JLabel h = Theme.label(heading, Theme.font(Font.BOLD, 22), Color.WHITE);
        JLabel id = Theme.label("Booking ID  " + b.getId() + "   \u2022   " + b.getCreatedAt().format(DateUtil.DATE_TIME),
                Theme.SMALL, Theme.GOLD);
        h.setAlignmentX(LEFT_ALIGNMENT);
        id.setAlignmentX(LEFT_ALIGNMENT);
        head.add(h);
        head.add(Box.createVerticalStrut(4));
        head.add(id);
        root.add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(18, 28, 8, 28));
        int r = 0;
        r = section(body, r, "GUEST");
        r = row(body, r, "Name", b.getGuest().getName());
        r = row(body, r, "Phone", b.getGuest().getPhone());
        r = row(body, r, "Email", b.getGuest().getEmail());
        r = section(body, r, "STAY");
        r = row(body, r, "Room", b.getRoomNumber() + "  \u00B7  " + b.getRoomType().getLabel());
        r = row(body, r, "Check-in", b.getCheckIn().format(DateUtil.DISPLAY));
        r = row(body, r, "Check-out", b.getCheckOut().format(DateUtil.DISPLAY));
        r = row(body, r, "Nights / Guests", b.getNights() + " night(s)  /  " + b.getGuestCount() + " guest(s)");
        r = row(body, r, "Status", b.effectiveStatus().getLabel());
        r = section(body, r, "PAYMENT");
        r = row(body, r, "Subtotal", Theme.moneyExact(b.getSubtotal()));
        r = row(body, r, "GST (12%)", Theme.moneyExact(b.getTax()));
        r = row(body, r, "Total", Theme.moneyExact(b.getTotal()));
        r = row(body, r, "Method", b.getPayment().getMethod().getLabel());
        r = row(body, r, "Payment status", b.getPayment().getStatus().getLabel());
        r = row(body, r, "Transaction ID", b.getPayment().getTransactionId());
        if (b.getRefundAmount() > 0) r = row(body, r, "Refund issued", Theme.moneyExact(b.getRefundAmount()));
        root.add(body, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        foot.setOpaque(false);
        JButton export = Theme.ghost("Save Receipt (.txt)");
        JButton close = Theme.gold("Close");
        export.addActionListener(e -> {
            try {
                Path p = ReceiptExporter.export(b);
                Theme.toast(this, "Receipt saved to:\n" + p, "Receipt saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                Theme.toast(this, "Could not save receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        close.addActionListener(e -> dispose());
        foot.add(export);
        foot.add(close);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(Math.max(getWidth(), 480), getHeight());
        setLocationRelativeTo(owner);
    }

    private int section(JPanel p, int row, String title) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(row == 0 ? 0 : 14, 0, 4, 0);
        p.add(Theme.label(title, Theme.font(Font.BOLD, 11), Theme.GOLD_DARK), c);
        return row + 1;
    }

    private int row(JPanel p, int row, String k, String v) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(3, 0, 3, 24);
        c.gridx = 0;
        p.add(Theme.label(k, Theme.BODY, Theme.MUTED), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(3, 0, 3, 0);
        p.add(Theme.label(v, Theme.BOLD, Theme.TEXT), c);
        return row + 1;
    }
}
