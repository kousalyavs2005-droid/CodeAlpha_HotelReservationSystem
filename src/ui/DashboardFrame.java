package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main window: navy sidebar + page area. */
public class DashboardFrame extends JFrame {
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final JLabel pageTitle = Theme.label("Dashboard", Theme.TITLE, Theme.NAVY);
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private final Map<String, Refreshable> refreshables = new LinkedHashMap<>();

    public DashboardFrame() {
        super("Grand Aurora - Hotel Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BG);
        right.add(topBar(), BorderLayout.NORTH);
        pageHost.setOpaque(false);
        right.add(pageHost, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        addPage("Dashboard", new DashboardPanel());
        addPage("Search & Book", new SearchPanel());
        addPage("Reservations", new BookingsPanel());
        addPage("Rooms", new RoomsPanel());
        show("Dashboard");
    }

    private void addPage(String name, JPanel panel) {
        pageHost.add(panel, name);
        refreshables.put(name, (Refreshable) panel);
        NavButton b = new NavButton(name);
        b.addActionListener(e -> show(name));
        navButtons.put(name, b);
        navHost.add(b);
        navHost.add(Box.createVerticalStrut(4));
    }

    private final JPanel navHost = new JPanel();

    private JPanel sidebar() {
        JPanel s = new JPanel(new BorderLayout());
        s.setBackground(Theme.NAVY);
        s.setPreferredSize(new Dimension(240, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(28, 24, 24, 24));
        JLabel n = Theme.label("GRAND AURORA", Theme.font(Font.BOLD, 18), Color.WHITE);
        JLabel sub = Theme.label("HOTEL & RESORT", Theme.font(Font.PLAIN, 11), Theme.GOLD);
        n.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(n);
        brand.add(sub);
        s.add(brand, BorderLayout.NORTH);

        navHost.setOpaque(false);
        navHost.setLayout(new BoxLayout(navHost, BoxLayout.Y_AXIS));
        navHost.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
        s.add(navHost, BorderLayout.CENTER);

        JButton out = Theme.ghost("Sign out");
        out.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(BorderFactory.createEmptyBorder(0, 16, 20, 16));
        foot.add(out);
        s.add(foot, BorderLayout.SOUTH);
        return s;
    }

    private JPanel topBar() {
        JPanel t = new JPanel(new BorderLayout());
        t.setBackground(Theme.BG);
        t.setBorder(BorderFactory.createEmptyBorder(22, 24, 0, 24));
        t.add(pageTitle, BorderLayout.WEST);
        t.add(Theme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")), Theme.BODY, Theme.MUTED),
                BorderLayout.EAST);
        return t;
    }

    private void show(String name) {
        pageTitle.setText(name);
        for (Map.Entry<String, NavButton> e : navButtons.entrySet()) e.getValue().setActive(e.getKey().equals(name));
        refreshables.get(name).refresh();
        pages.show(pageHost, name);
    }

    /** Sidebar item with a gold accent when active. */
    private static class NavButton extends JButton {
        private boolean active;

        NavButton(String text) {
            super(text);
            setFont(Theme.BOLD);
            setForeground(new Color(0xC7CEDA));
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 12));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        }

        void setActive(boolean a) {
            active = a;
            setForeground(a ? Color.WHITE : new Color(0xC7CEDA));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.NAVY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Theme.GOLD);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
