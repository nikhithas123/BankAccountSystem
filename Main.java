import javax.swing.*;

/**
 * Main.java
 * ---------------------------------------------------------
 * ENTRY POINT
 *
 * Every Java application starts execution from a "main" method.
 * This class's only job is to safely launch the Swing GUI.
 *
 * Why SwingUtilities.invokeLater()?
 * Swing is NOT thread-safe. All GUI creation and updates must
 * happen on a special background thread called the "Event
 * Dispatch Thread" (EDT). If we built the GUI directly inside
 * main() (which runs on the default "main" thread), we could
 * get subtle, hard-to-debug rendering bugs. invokeLater()
 * schedules our GUI-building code to run correctly on the EDT.
 * This is the standard, recommended way to start any Swing app.
 * ---------------------------------------------------------
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankManagementGUI gui = new BankManagementGUI();
            gui.setVisible(true); // makes the window actually appear on screen
        });
    }
}
