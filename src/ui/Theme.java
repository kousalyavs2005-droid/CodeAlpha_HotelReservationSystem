package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Central place for the premium look: navy + gold palette, fonts and small reusable components. */
public final class Theme {
    public static final Color NAVY = new Color(0x0F1B2D);
    public static final Color NAVY_LIGHT = new Color(0x1B2B45);
    public static final Color GOLD = new Color(0xC9A24B);
    public static final Color GOLD_DARK = new Color(0xA9852F);
    public static final Color BG = new Color(0xF5F3EE);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE5E1D8);
    public static final Color SUCCESS = new Color(0x2E9E6B);
    public static final Color DANGER = new Color(0xC0392B);
    public static final Color WARNING = new Color(0xD68910);

    public static final Font TITLE = font(Font.BOLD, 26);
    public static final Font HEADING = font(Font.BOLD, 18);
    public static final Font BODY = font(Font.PLAIN, 14);
    public static final Font BOLD = font(Font.BOLD, 14);
    public static final Font SMALL = font(Font.PLAIN, 12);

    private Theme() { }

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BOLD);
        UIManager.put("Label.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("Spinner.font", BODY);
    }

    public static Color hex(String h) { return Color.decode(h); }

    public static String money(double v) { return String.format("\u20B9%,.0f", v); }
    public static String moneyExact(double v) { return String.format("\u20B9%,.2f", v); }

    // ---------- factories ----------
    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    public static JTextField field() {
        JTextField t = new JTextField();
        styleInput(t);
        return t;
    }

    public static void styleInput(JComponent c) {
        c.setFont(BODY);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, 38));
    }

    public static JComboBox<Object> combo(Object... items) {
        JComboBox<Object> cb = new JComboBox<>(items);
        cb.setFont(BODY);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 38));
        return cb;
    }

    public static JButton gold(String text) { return new RoundedButton(text, GOLD, GOLD_DARK, NAVY); }
    public static JButton navy(String text) { return new RoundedButton(text, NAVY, NAVY_LIGHT, Color.WHITE); }
    public static JButton danger(String text) { return new RoundedButton(text, DANGER, DANGER.darker(), Color.WHITE); }
    public static JButton ghost(String text) { return new RoundedButton(text, new Color(0xEDE9DF), new Color(0xE0DBCD), TEXT); }

    public static void styleTable(JTable t) {
        t.setFont(BODY);
        t.setRowHeight(40);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEFEBE2));
        t.setSelectionBackground(new Color(0xF6EBCB));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader h = t.getTableHeader();
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setBackground(NAVY);
                l.setForeground(Color.WHITE);
                l.setFont(font(Font.BOLD, 13));
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return l;
            }
        });
        DefaultTableCellRenderer padded = new DefaultTableCellRenderer();
        padded.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, false, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!s) l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFBFAF7));
                return l;
            }
        });
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static void toast(Component parent, String message, String title, int type) {
        JOptionPane.showMessageDialog(parent, message, title, type);
    }

    // ---------- components ----------
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final boolean border;

        public RoundedPanel(int radius, Color fill, boolean border) {
            this.radius = radius;
            this.fill = fill;
            this.border = border;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (border) {
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(1, 3, getWidth() - 2, getHeight() - 3, radius, radius);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - (border ? 3 : 1), radius, radius);
            if (border) {
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class RoundedButton extends JButton {
        private final Color base, hover, fg;
        private boolean over;

        RoundedButton(String text, Color base, Color hover, Color fg) {
            super(text);
            this.base = base;
            this.hover = hover;
            this.fg = fg;
            setFont(BOLD);
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xD9D5CB) : over ? hover : base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            setForeground(isEnabled() ? fg : MUTED);
            super.paintComponent(g);
        }
    }

    /** Small coloured pill, e.g. the room category badge. */
    public static class Badge extends JLabel {
        private final Color color;

        public Badge(String text, Color color) {
            super(text, SwingConstants.CENTER);
            this.color = color;
            setFont(font(Font.BOLD, 11));
            setForeground(Color.WHITE);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
