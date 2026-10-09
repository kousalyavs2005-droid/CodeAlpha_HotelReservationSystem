import ui.LoginFrame;
import ui.Theme;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
