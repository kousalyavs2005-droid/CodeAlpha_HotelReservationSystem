package ui;

import exception.InvalidDateException;
import exception.PaymentException;
import exception.RoomNotAvailableException;
import model.Booking;
import model.Guest;
import model.PaymentMethod;
import model.Room;
import service.BookingService;
import service.PaymentRequest;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import util.DateUtil;

/** Two-step dialog: 1) guest details + price summary, 2) payment simulation. */
public class BookingDialog extends JDialog {
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final BookingService.Quote quote;

    private final JTextField nameField = Theme.field();
    private final JTextField phoneField = Theme.field();
    private final JTextField emailField = Theme.field();
    private final JSpinner guestSpinner;

    private final JComboBox<Object> methodBox = Theme.combo((Object[]) PaymentMethod.values());
    private final JTextField cardNumber = Theme.field();
    private final JTextField cardHolder = Theme.field();
    private final JTextField cardExpiry = Theme.field();
    private final JPasswordField cardCvv = new JPasswordField();
    private final JTextField upiField = Theme.field();

    private final CardLayout steps = new CardLayout();
    private final JPanel stepHost = new JPanel(steps);
    private final JLabel stepLabel = Theme.label("Step 1 of 2  \u00B7  Guest details", Theme.SMALL, Theme.GOLD);
    private final JLabel status = Theme.label(" ", Theme.SMALL, Theme.DANGER);
    private final JButton back = Theme.ghost("Back");
    private final JButton next = Theme.gold("Continue to Payment");

    private Booking result;
    private boolean onPaymentStep;

    public BookingDialog(Window owner, Room room, LocalDate checkIn, LocalDate checkOut, int guests) {
        super(owner, "Reserve Room " + room.getNumber(), ModalityType.APPLICATION_MODAL);
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.quote = AppContext.BOOKINGS.quote(room, checkIn, checkOut);
        this.guestSpinner = new JSpinner(new SpinnerNumberModel(Math.min(guests, room.getCapacity()), 1, room.getCapacity(), 1));
        Theme.styleInput(guestSpinner);
        Theme.styleInput(cardCvv);
        cardNumber.setToolTipText("Test card: 4111 1111 1111 1111");

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(header(), BorderLayout.NORTH);

        stepHost.setOpaque(false);
        stepHost.add(detailsStep(), "details");
        stepHost.add(paymentStep(), "payment");
        root.add(stepHost, BorderLayout.CENTER);
        root.add(footer(), BorderLayout.SOUTH);

        setContentPane(root);
        setSize(760, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** Shows the dialog and returns the confirmed booking, or null if the guest closed it. */
    public Booking showDialog() {
        setVisible(true);
        return result;
    }

    // ------------------------------------------------------------------ layout
    private JPanel header() {
        JPanel h = new JPanel();
        h.setBackground(Theme.NAVY);
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setBorder(BorderFactory.createEmptyBorder(18, 28, 16, 28));
        JLabel t = Theme.label("Reserve Room " + room.getNumber() + "  \u00B7  " + room.getType().getLabel(),
                Theme.font(Font.BOLD, 21), Color.WHITE);
        t.setAlignmentX(LEFT_ALIGNMENT);
        stepLabel.setAlignmentX(LEFT_ALIGNMENT);
        h.add(t);
        h.add(Box.createVerticalStrut(4));
        h.add(stepLabel);
        return h;
    }

    private JPanel detailsStep() {
        JPanel p = new JPanel(new BorderLayout(20, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        addRow(form, 0, "Full name", nameField);
        addRow(form, 1, "Phone (10 digits)", phoneField);
        addRow(form, 2, "Email", emailField);
        addRow(form, 3, "Number of guests (max " + room.getCapacity() + ")", guestSpinner);
        p.add(form, BorderLayout.CENTER);
        p.add(summaryCard(), BorderLayout.EAST);
        return p;
    }

    private JPanel summaryCard() {
        Theme.RoundedPanel card = new Theme.RoundedPanel(16, Theme.CARD, true);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        card.setPreferredSize(new Dimension(270, 0));
        int r = 0;
        r = sum(card, r, "Check-in", checkIn.format(DateUtil.DISPLAY), false);
        r = sum(card, r, "Check-out", checkOut.format(DateUtil.DISPLAY), false);
        r = sum(card, r, "Nights", String.valueOf(quote.nights), false);
        r = sum(card, r, "Rate / night", Theme.money(quote.rate), false);
        r = sum(card, r, "Subtotal", Theme.moneyExact(quote.subtotal), false);
        r = sum(card, r, "GST (12%)", Theme.moneyExact(quote.tax), false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = r; c.gridx = 0; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(8, 0, 8, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        card.add(sep, c);
        sum(card, r + 1, "Total", Theme.moneyExact(quote.total), true);
        GridBagConstraints fill = new GridBagConstraints();
        fill.gridy = r + 2; fill.weighty = 1;
        card.add(Box.createVerticalGlue(), fill);
        return card;
    }

    private int sum(JPanel p, int row, String k, String v, boolean big) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.insets = new Insets(4, 0, 4, 0);
        c.gridx = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        p.add(Theme.label(k, big ? Theme.BOLD : Theme.BODY, big ? Theme.TEXT : Theme.MUTED), c);
        c.gridx = 1; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        p.add(Theme.label(v, big ? Theme.font(Font.BOLD, 17) : Theme.BOLD, big ? Theme.GOLD_DARK : Theme.TEXT), c);
        return row + 1;
    }

    private JPanel paymentStep() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        addRow(top, 0, "Payment method", methodBox);
        p.add(top, BorderLayout.NORTH);

        CardLayout inner = new CardLayout();
        JPanel fields = new JPanel(inner);
        fields.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        addRow(card, 0, "Card number", cardNumber);
        addRow(card, 1, "Name on card", cardHolder);
        addRow(card, 2, "Expiry (MM/YY)", cardExpiry);
        addRow(card, 3, "CVV", cardCvv);
        fields.add(card, PaymentMethod.CARD.name());

        JPanel upi = new JPanel(new GridBagLayout());
        upi.setOpaque(false);
        addRow(upi, 0, "UPI ID", upiField);
        fields.add(upi, PaymentMethod.UPI.name());

        JPanel cash = new JPanel(new BorderLayout());
        cash.setOpaque(false);
        cash.add(Theme.label("<html>No payment is taken now. The full amount is collected in cash at check-in.<br>"
                + "Cancelling a pay-at-hotel booking is always free.</html>", Theme.BODY, Theme.MUTED), BorderLayout.NORTH);
        fields.add(cash, PaymentMethod.CASH.name());

        methodBox.addActionListener(e -> inner.show(fields, ((PaymentMethod) methodBox.getSelectedItem()).name()));
        inner.show(fields, PaymentMethod.CARD.name());
        p.add(fields, BorderLayout.CENTER);

        JLabel due = Theme.label("Amount due: " + Theme.moneyExact(quote.total) + "   (simulated payment - test card 4111 1111 1111 1111)",
                Theme.BOLD, Theme.GOLD_DARK);
        p.add(due, BorderLayout.SOUTH);
        return p;
    }

    private JPanel footer() {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(4, 28, 14, 28));
        status.setHorizontalAlignment(SwingConstants.LEFT);
        f.add(status, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = Theme.ghost("Cancel");
        cancel.addActionListener(e -> dispose());
        back.setVisible(false);
        back.addActionListener(e -> goTo(false));
        next.addActionListener(e -> onNext());
        buttons.add(cancel);
        buttons.add(back);
        buttons.add(next);
        f.add(buttons, BorderLayout.EAST);
        return f;
    }

    private void addRow(JPanel p, int row, String label, JComponent comp) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row * 2; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(row == 0 ? 0 : 10, 0, 3, 0);
        p.add(Theme.label(label, Theme.BOLD, Theme.TEXT), c);
        c.gridy = row * 2 + 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 0, 0);
        p.add(comp, c);
    }

    // ------------------------------------------------------------------ behaviour
    private void goTo(boolean payment) {
        onPaymentStep = payment;
        steps.show(stepHost, payment ? "payment" : "details");
        stepLabel.setText(payment ? "Step 2 of 2  \u00B7  Payment" : "Step 1 of 2  \u00B7  Guest details");
        back.setVisible(payment);
        next.setText(payment ? "Pay & Confirm" : "Continue to Payment");
        status.setText(" ");
    }

    private void onNext() {
        if (!onPaymentStep) {
            String err = validateGuest();
            if (err != null) { status.setText(err); return; }
            goTo(true);
        } else {
            confirmAndPay();
        }
    }

    private String validateGuest() {
        if (nameField.getText().trim().length() < 3) return "Please enter the guest's full name.";
        if (!phoneField.getText().trim().matches("\\d{10}")) return "Phone number must be exactly 10 digits.";
        if (!emailField.getText().trim().matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+")) return "Please enter a valid email address.";
        return null;
    }

    private void confirmAndPay() {
        Guest guest = new Guest(nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim());
        PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
        PaymentRequest req = new PaymentRequest(method, cardNumber.getText(), cardHolder.getText(),
                cardExpiry.getText(), new String(cardCvv.getPassword()), upiField.getText());
        int guests = (Integer) guestSpinner.getValue();

        next.setEnabled(false);
        back.setEnabled(false);
        status.setForeground(Theme.MUTED);
        status.setText(method == PaymentMethod.CASH ? "Confirming reservation..." : "Processing payment, please wait...");

        new SwingWorker<Booking, Void>() {
            @Override protected Booking doInBackground() throws Exception {
                return AppContext.BOOKINGS.book(guest, room.getNumber(), checkIn, checkOut, guests, req);
            }

            @Override protected void done() {
                try {
                    result = get();
                    dispose();
                    new BookingDetailsDialog(getOwner(), result, "Booking Confirmed").setVisible(true);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    status.setForeground(Theme.DANGER);
                    if (cause instanceof PaymentException || cause instanceof RoomNotAvailableException
                            || cause instanceof InvalidDateException) {
                        status.setText("<html>" + cause.getMessage() + "</html>");
                    } else {
                        status.setText("Unexpected error: " + cause);
                    }
                    next.setEnabled(true);
                    back.setEnabled(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
