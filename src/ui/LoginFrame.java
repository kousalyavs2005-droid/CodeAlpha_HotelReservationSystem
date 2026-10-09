package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Sign-in screen: the hotel lobby photo fills the window, the brand sits on the
 * left and a dark glass card with the form floats on the right.
 */
public class LoginFrame extends JFrame {

    private static final String USER = "admin";
    private static final String PASS = "admin123";

    // Palette taken from the lobby photo: black marble, brushed gold, night blue.
    private static final Color GOLD = new Color(0xD4AF5A);
    private static final Color GOLD_LIGHT = new Color(0xE9CC84);
    private static final Color GOLD_DEEP = new Color(0xB38B34);
    private static final Color INK = new Color(0x0A0D14);
    private static final Color TEXT_SOFT = new Color(0xCFCABD);
    private static final Color ERROR = new Color(0xFF8F80);

    private final InputField userField = new InputField("Enter your username");
    private final PasswordInput passField = new PasswordInput("Enter your password");
    private final JLabel error = new JLabel(" ");

    public LoginFrame() {
        super("Grand Aurora - Sign in");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setResizable(true);

        LobbyBackground root = new LobbyBackground();
        root.add(brandPanel(), BorderLayout.CENTER);
        root.add(formWrapper(), BorderLayout.EAST);
        setContentPane(root);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                userField.requestFocusInWindow();
            }
        });
    }

    // ------------------------------------------------------------------
    // Left: brand
    // ------------------------------------------------------------------

    private JPanel brandPanel() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel stars = styled("\u2605  \u2605  \u2605  \u2605  \u2605",
                Theme.font(Font.PLAIN, 14), GOLD);
        JLabel name = trackedLabel("GRAND AURORA", serif(Font.BOLD, 46), 0.10f, Color.WHITE);
        JLabel sub = trackedLabel("HOTEL & RESORT", Theme.font(Font.PLAIN, 14), 0.38f, GOLD);
        JLabel tag = styled("<html>Reservation management,<br>"
                        + "crafted for effortless hospitality.</html>",
                Theme.font(Font.PLAIN, 16), TEXT_SOFT);

        for (JComponent c : new JComponent[]{stars, name, sub, tag}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        box.add(stars);
        box.add(Box.createVerticalStrut(16));
        box.add(name);
        box.add(Box.createVerticalStrut(4));
        box.add(sub);
        box.add(Box.createVerticalStrut(22));
        GoldRule rule = new GoldRule(64);
        rule.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(rule);
        box.add(Box.createVerticalStrut(22));
        box.add(tag);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(0, 72, 72, 0);
        wrap.add(box, c);
        return wrap;
    }

    // ------------------------------------------------------------------
    // Right: glass card with the form
    // ------------------------------------------------------------------

    private JPanel formWrapper() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(540, 0));
        wrap.add(formCard());
        return wrap;
    }

    private JPanel formCard() {
        GlassCard card = new GlassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 38, 34, 38));
        card.setPreferredSize(new Dimension(420, 500));

        JLabel hi = styled("Welcome back", serif(Font.BOLD, 30), Color.WHITE);
        JLabel sub = styled("Sign in to the reservation desk",
                Theme.font(Font.PLAIN, 14), TEXT_SOFT);

        JLabel userLabel = styled("Username", Theme.font(Font.BOLD, 13), TEXT_SOFT);
        JLabel passLabel = styled("Password", Theme.font(Font.BOLD, 13), TEXT_SOFT);

        error.setFont(Theme.font(Font.PLAIN, 13));
        error.setForeground(ERROR);

        JButton go = new GoldButton("Sign in");
        go.addActionListener(e -> attempt());
        userField.addActionListener(e -> passField.requestFocusInWindow());
        passField.addActionListener(e -> attempt());

        JLabel foot = styled("Authorised staff only.",
                Theme.font(Font.PLAIN, 12), new Color(0x9A9588));

        Component[] rows = {
                hi,
                Box.createVerticalStrut(10),
                sub,
                Box.createVerticalStrut(18),
                new GoldRule(40),
                Box.createVerticalStrut(28),
                userLabel,
                Box.createVerticalStrut(8),
                userField,
                Box.createVerticalStrut(18),
                passLabel,
                Box.createVerticalStrut(8),
                passField,
                Box.createVerticalStrut(10),
                error,
                Box.createVerticalStrut(14),
                go,
                Box.createVerticalGlue(),
                foot
        };

        for (Component row : rows) {
            if (row instanceof JComponent) {
                JComponent jc = (JComponent) row;
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
                if (row instanceof InputField || row instanceof PasswordInput
                        || row instanceof GoldButton) {
                    jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                    jc.setPreferredSize(new Dimension(10, 48));
                }
            }
            card.add(row);
        }
        return card;
    }

    // ------------------------------------------------------------------
    // Login check
    // ------------------------------------------------------------------

    private void attempt() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (USER.equals(username) && PASS.equals(password)) {
            error.setText(" ");
            dispose();
            new DashboardFrame().setVisible(true);
        } else {
            error.setText("Incorrect username or password.");
            passField.setText("");
            passField.requestFocusInWindow();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static JLabel styled(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private static Font serif(int style, int size) {
        return new Font("Georgia", style, size);
    }

    /** Label whose preferred width includes the extra width letter-spacing adds. */
    private static JLabel trackedLabel(String text, Font base, float tracking, Color c) {
        final int pad = Math.round(base.getSize2D() * tracking * text.length()) + 8;
        JLabel l = new JLabel(text) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + pad, d.height);
            }
        };
        l.setFont(tracked(base, tracking));
        l.setForeground(c);
        return l;
    }

    private static Font tracked(Font f, float tracking) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Looks for the lobby photo on the classpath, then on disk. */
    private static BufferedImage loadLobbyImage() {
        String[] names = {"/resources/hotel_lobby.png", "/resources/hotel_lobby.png.png"};
        for (String n : names) {
            URL url = LoginFrame.class.getResource(n);
            if (url != null) {
                try { return ImageIO.read(url); } catch (Exception ignored) { }
            }
        }
        String[] files = {"src/resources/hotel_lobby.png", "src/resources/hotel_lobby.png.png"};
        for (String f : files) {
            File file = new File(f);
            if (file.exists()) {
                try { return ImageIO.read(file); } catch (Exception ignored) { }
            }
        }
        System.out.println("hotel_lobby.png not found - using plain background.");
        return null;
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    /** Full-window lobby photo, scaled to cover, with shading for legibility. */
    private static class LobbyBackground extends JPanel {
        private final BufferedImage image = loadLobbyImage();

        LobbyBackground() {
            super(new BorderLayout());
            setBackground(INK);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();

            if (image != null) {
                double ir = (double) image.getWidth() / image.getHeight();
                double pr = (double) w / h;
                int dw, dh;
                if (ir > pr) { dh = h; dw = (int) (h * ir); }
                else { dw = w; dh = (int) (w / ir); }
                g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            }

            // Darken toward the right so the form card reads clearly.
            g2.setPaint(new LinearGradientPaint(0, 0, w, 0,
                    new float[]{0f, 0.45f, 1f},
                    new Color[]{new Color(5, 8, 14, 70), new Color(5, 8, 14, 100), new Color(5, 8, 14, 225)}));
            g2.fillRect(0, 0, w, h);

            // Darken the lower left behind the brand name.
            g2.setPaint(new GradientPaint(0, h * 0.45f, new Color(5, 8, 14, 0),
                    0, h, new Color(5, 8, 14, 190)));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /** Translucent dark card with a thin gold edge. */
    private static class GlassCard extends JPanel {
        GlassCard() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(10, 13, 20, 214));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 22, 22);
            g2.setColor(new Color(212, 175, 90, 110));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 22, 22);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Short gold line under headings. */
    private static class GoldRule extends JComponent {
        private final int length;

        GoldRule(int length) {
            this.length = length;
            setOpaque(false);
            Dimension d = new Dimension(length, 2);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, GOLD_LIGHT, length, 0, GOLD_DEEP));
            g2.fillRect(0, 0, length, 2);
            g2.dispose();
        }
    }

    /** Shared painting for the dark text inputs. */
    private static void paintInput(Graphics g, JTextComponent c, String hint, boolean focused) {
        Graphics2D g2 = (Graphics2D) g.create();
        smooth(g2);
        int w = c.getWidth(), h = c.getHeight();
        g2.setColor(new Color(255, 255, 255, focused ? 26 : 16));
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
        if (focused) {
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(1.6f));
        } else {
            g2.setColor(new Color(255, 255, 255, 56));
        }
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
        g2.dispose();
    }

    private static void paintHint(Graphics g, JTextComponent c, String hint) {
        if (!c.getText().isEmpty() || hint == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        smooth(g2);
        g2.setFont(c.getFont());
        g2.setColor(new Color(255, 255, 255, 95));
        FontMetrics fm = g2.getFontMetrics();
        Insets in = c.getInsets();
        int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(hint, in.left, y);
        g2.dispose();
    }

    private static void styleInput(JTextComponent c) {
        c.setOpaque(false);
        c.setFont(Theme.font(Font.PLAIN, 15));
        c.setForeground(Color.WHITE);
        c.setCaretColor(GOLD_LIGHT);
        c.setSelectionColor(new Color(212, 175, 90, 140));
        c.setSelectedTextColor(Color.WHITE);
        c.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { c.repaint(); }
            @Override public void focusLost(FocusEvent e) { c.repaint(); }
        });
    }

    private static class InputField extends JTextField {
        private final String hint;

        InputField(String hint) {
            this.hint = hint;
            styleInput(this);
        }

        @Override protected void paintComponent(Graphics g) {
            paintInput(g, this, hint, hasFocus());
            super.paintComponent(g);
            paintHint(g, this, hint);
        }
    }

    private static class PasswordInput extends JPasswordField {
        private final String hint;

        PasswordInput(String hint) {
            this.hint = hint;
            styleInput(this);
            setEchoChar('\u2022');
        }

        @Override protected void paintComponent(Graphics g) {
            paintInput(g, this, hint, hasFocus());
            super.paintComponent(g);
            if (getPassword().length == 0) paintHint(g, this, hint);
        }
    }

    /** Gold gradient call-to-action. */
    private static class GoldButton extends JButton {
        private boolean over;

        GoldButton(String text) {
            super(text);
            setFont(Theme.font(Font.BOLD, 15));
            setForeground(INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            Color top = over ? GOLD_LIGHT.brighter() : GOLD_LIGHT;
            Color bottom = getModel().isPressed() ? GOLD_DEEP.darker() : (over ? GOLD : GOLD_DEEP);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRoundRect(0, 0, w, h, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
