import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * BankManagementGUI.java
 * ---------------------------------------------------------
 * VIEW / CONTROLLER CLASS
 *
 * This is the ONLY class that talks to Swing. It builds the
 * window, arranges the components, listens for button clicks,
 * validates raw text input, and calls into BankAccountManager
 * (the backend) to actually perform the banking operations.
 *
 * Layout approach: the entire card body uses ONE GridBagLayout
 * instead of nested BoxLayouts. GridBagLayout lets us place any
 * component at any (column, row) with its own width span, fill
 * behavior, and weight -- which is exactly what a two-column
 * dashboard form needs (e.g. Name + Account Number side by side,
 * Deposit + Withdraw side by side, while the title, Initial
 * Balance, Create Account button, Balance display, and footer
 * buttons each span both columns).
 * ---------------------------------------------------------
 */
public class BankManagementGUI extends JFrame {

    // ----------------- BACKEND (business logic) -----------------
    // The GUI class HOLDS a reference to the manager but does not
    // extend it. This is "composition": "a GUI HAS-A manager"
    // rather than "a GUI IS-A manager".
    private final BankAccountManager bankManager = new BankAccountManager();

    // Tracks which account is currently "active" in the GUI, i.e.
    // the account most recently created, so Deposit/Withdraw/Balance
    // buttons know which account to operate on without asking the
    // user to retype the account number every time.
    private String activeAccountNumber = null;

    // ----------------- COLOR PALETTE (modern blue/purple accent) -----------------
    private static final Color WINDOW_BG    = new Color(0xF1, 0xF3, 0xF6); // light gray
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x5B, 0x4B, 0xE8); // blue-purple
    private static final Color ACCENT_DARK  = new Color(0x47, 0x38, 0xC7); // hover shade
    private static final Color TEXT_DARK    = new Color(0x25, 0x27, 0x33);
    private static final Color TEXT_MUTED   = new Color(0x8A, 0x8F, 0x9C);
    private static final Color DANGER       = new Color(0xE4, 0x5B, 0x5B);
    private static final Color FIELD_BG     = new Color(0xF6, 0xF7, 0xFB);
    private static final Color FIELD_BORDER = new Color(0xDF, 0xE2, 0xEA);

    // Fonts - modern, cross-platform fallback.
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 24);
    private static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_FIELD   = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BUTTON  = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_BALANCE = new Font("SansSerif", Font.BOLD, 28);

    // ----------------- INPUT FIELDS (need to be accessed by multiple methods) -----------------
    private JTextField nameField;
    private JTextField accNumberField;
    private JTextField initialBalanceField;
    private JTextField depositField;
    private JTextField withdrawField;
    private JTextField checkBalanceAccNumberField; // account number typed for the Check Balance lookup
    private JLabel balanceValueLabel; // shows "Rs. 0.00" and updates live

    /**
     * Constructor: builds the entire window when a BankManagementGUI object
     * is created. All GUI setup calls happen from here.
     */
    public BankManagementGUI() {
        // ----- JFrame basic setup -----
        setTitle("Bank Account Management System");
        setSize(1040, 720);
        setMinimumSize(new Dimension(940, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // clicking X closes the app
        setLocationRelativeTo(null); // centers the window on the screen

        // ----- Root background panel -----
        // getContentPane() uses GridBagLayout with NO constraints added,
        // which is Swing's simplest way to perfectly center one single
        // child (the card) both horizontally and vertically, regardless
        // of how the window is resized.
        getContentPane().setBackground(WINDOW_BG);
        getContentPane().setLayout(new GridBagLayout());

        // ----- The rounded white card (see RoundedPanel class below) -----
        RoundedPanel card = new RoundedPanel(24, CARD_BG);
        card.setPreferredSize(new Dimension(880, 640));
        card.setBorder(new EmptyBorder(34, 48, 34, 48)); // inner padding (top,left,bottom,right)

        // The card itself uses GridBagLayout so every row of the form
        // can freely mix full-width components (title, Initial Balance,
        // Create Account button, Balance display, footer) with two-column
        // rows (Name/Account Number, Deposit/Withdraw).
        card.setLayout(new GridBagLayout());
        buildFormInto(card);

        getContentPane().add(card, new GridBagConstraints());
    }

    // =========================================================
    //  FORM BUILDER
    //  Adds every row of the form into the card's GridBagLayout.
    //  Two logical columns exist: LEFT_COL (x=0) and RIGHT_COL (x=1).
    //  Full-width rows use gridwidth = 2 to span both columns.
    // =========================================================

    private static final int LEFT_COL = 0;
    private static final int RIGHT_COL = 1;
    private static final int FULL_WIDTH = 2;

    private void buildFormInto(JPanel card) {
        int row = 0;

        // ---- Title (spans both columns, centered) ----
        JLabel title = new JLabel("Bank Account Management System");
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 22, 0)));
        row++;

        // ---- Section: Create Account ----
        card.add(sectionHeader("Create Account"),
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 10, 0)));
        row++;

        // Account Holder Name (left) + Account Number (right) - side by side
        card.add(fieldLabel("Account Holder Name"),
                gbc(LEFT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 0, 4, 10)));
        card.add(fieldLabel("Account Number"),
                gbc(RIGHT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 10, 4, 0)));
        row++;

        nameField = styledTextField();
        accNumberField = styledTextField();
        card.add(nameField, gbc(LEFT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 0, 14, 10)));
        card.add(accNumberField, gbc(RIGHT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 10, 14, 0)));
        row++;

        // Initial Balance (full width, own row)
        card.add(fieldLabel("Initial Balance (Rs.)"),
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 4, 0)));
        row++;

        initialBalanceField = styledTextField();
        card.add(initialBalanceField,
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 16, 0)));
        row++;

        RoundedButton createBtn = new RoundedButton("Create Account", ACCENT, ACCENT_DARK, Color.WHITE);
        createBtn.addActionListener(e -> handleCreateAccount());
        card.add(createBtn, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.NONE, 1.0, insets(0, 0, 22, 0)));
        row++;

        // ---- Divider ----
        card.add(new JSeparator(),
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 22, 0)));
        row++;

        // ---- Deposit (left column) + Withdraw (right column) side by side ----
        card.add(sectionHeader("Deposit Money"),
                gbc(LEFT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 0, 10, 10)));
        card.add(sectionHeader("Withdraw Money"),
                gbc(RIGHT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 10, 10, 0)));
        row++;

        card.add(fieldLabel("Deposit Amount (Rs.)"),
                gbc(LEFT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 0, 4, 10)));
        card.add(fieldLabel("Withdraw Amount (Rs.)"),
                gbc(RIGHT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 10, 4, 0)));
        row++;

        depositField = styledTextField();
        withdrawField = styledTextField();
        card.add(depositField, gbc(LEFT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 0, 14, 10)));
        card.add(withdrawField, gbc(RIGHT_COL, row, 1, GridBagConstraints.HORIZONTAL, 0.5, insets(0, 10, 14, 0)));
        row++;

        RoundedButton depositBtn = new RoundedButton("Deposit", ACCENT, ACCENT_DARK, Color.WHITE);
        depositBtn.addActionListener(e -> handleDeposit());
        // A different accent (danger tone) helps the user visually
        // distinguish "money out" from "money in" at a glance.
        RoundedButton withdrawBtn = new RoundedButton("Withdraw", DANGER, DANGER.darker(), Color.WHITE);
        withdrawBtn.addActionListener(e -> handleWithdraw());
        card.add(depositBtn, gbc(LEFT_COL, row, 1, GridBagConstraints.NONE, 0.5, insets(0, 0, 22, 10)));
        card.add(withdrawBtn, gbc(RIGHT_COL, row, 1, GridBagConstraints.NONE, 0.5, insets(0, 10, 22, 0)));
        row++;

        // ---- Divider ----
        card.add(new JSeparator(),
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 22, 0)));
        row++;

        // ---- Section: Current Balance (full width, centered) ----
        JLabel balanceHeader = sectionHeader("Current Balance");
        balanceHeader.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(balanceHeader, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 8, 0)));
        row++;

        // Account Number input specifically for the balance lookup.
        // This is intentionally separate from activeAccountNumber -- the
        // user must type the account number they want to check here.
        card.add(fieldLabel("Account Number"),
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 4, 0)));
        row++;

        checkBalanceAccNumberField = styledTextField();
        card.add(checkBalanceAccNumberField,
                gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 14, 0)));
        row++;

        balanceValueLabel = new JLabel("Rs. 0.00");
        balanceValueLabel.setFont(FONT_BALANCE);
        balanceValueLabel.setForeground(TEXT_DARK);
        balanceValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(balanceValueLabel, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.HORIZONTAL, 1.0, insets(0, 0, 14, 0)));
        row++;

        RoundedButton checkBtn = new RoundedButton("Check Balance", ACCENT, ACCENT_DARK, Color.WHITE);
        checkBtn.addActionListener(e -> handleCheckBalance());
        card.add(checkBtn, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.NONE, 1.0, insets(0, 0, 22, 0)));
        row++;

        // ---- Footer: Clear + Exit, side by side, centered ----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        footer.setOpaque(false);

        RoundedButton clearBtn = new RoundedButton("Clear", FIELD_BG, new Color(0xE9, 0xEB, 0xF2), TEXT_DARK);
        clearBtn.addActionListener(e -> handleClear());

        RoundedButton exitBtn = new RoundedButton("Exit", TEXT_DARK, Color.BLACK, Color.WHITE);
        exitBtn.addActionListener(e -> handleExit());

        footer.add(clearBtn);
        footer.add(exitBtn);
        card.add(footer, gbc(LEFT_COL, row, FULL_WIDTH, GridBagConstraints.NONE, 1.0, insets(0, 0, 0, 0)));
    }

    // =========================================================
    //  EVENT HANDLERS (unchanged from the working backend-connected version)
    // =========================================================

    /** Handles the "Create Account" button click. */
    private void handleCreateAccount() {
        try {
            String name = nameField.getText().trim();
            String accNumber = accNumberField.getText().trim();
            String balanceText = initialBalanceField.getText().trim();

            double initialBalance;
            try {
                initialBalance = Double.parseDouble(balanceText);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Initial balance must be a valid number.");
            }

            bankManager.createAccount(name, accNumber, initialBalance);

            activeAccountNumber = accNumber;
            balanceValueLabel.setText(formatCurrency(bankManager.getBalance(accNumber)));

            JOptionPane.showMessageDialog(this,
                    "Account created successfully for " + name + "\nAccount Number: " + accNumber,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /** Handles the "Deposit" button click. */
    private void handleDeposit() {
        try {
            ensureActiveAccount(); // makes sure an account exists first

            String depositText = depositField.getText().trim();
            double amount;
            try {
                amount = Double.parseDouble(depositText);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Deposit amount must be a valid number.");
            }

            bankManager.deposit(activeAccountNumber, amount);
            balanceValueLabel.setText(formatCurrency(bankManager.getBalance(activeAccountNumber)));

            JOptionPane.showMessageDialog(this,
                    "Deposit successful!\nNew Balance: " + formatCurrency(bankManager.getBalance(activeAccountNumber)),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            depositField.setText("");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /** Handles the "Withdraw" button click. */
    private void handleWithdraw() {
        try {
            ensureActiveAccount();

            String withdrawText = withdrawField.getText().trim();
            double amount;
            try {
                amount = Double.parseDouble(withdrawText);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Withdraw amount must be a valid number.");
            }

            // bankManager.withdraw() will throw IllegalArgumentException
            // if the amount is <= 0 OR if the balance is insufficient.
            bankManager.withdraw(activeAccountNumber, amount);
            balanceValueLabel.setText(formatCurrency(bankManager.getBalance(activeAccountNumber)));

            JOptionPane.showMessageDialog(this,
                    "Withdrawal successful!\nNew Balance: " + formatCurrency(bankManager.getBalance(activeAccountNumber)),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            withdrawField.setText("");

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /**
     * Handles the "Check Balance" button click.
     *
     * This does NOT use activeAccountNumber. Instead it reads whatever
     * account number the user typed into checkBalanceAccNumberField, so
     * this button can look up the balance of ANY existing account, not
     * just the one most recently created.
     */
    private void handleCheckBalance() {
        try {
            String accNumber = checkBalanceAccNumberField.getText().trim();

            if (accNumber.isEmpty()) {
                throw new IllegalArgumentException("Please enter an account number.");
            }

            // bankManager.getBalance() throws IllegalArgumentException if
            // no account exists with this number -- caught below and
            // shown as an error dialog, same as every other operation.
            double balance = bankManager.getBalance(accNumber);
            balanceValueLabel.setText(formatCurrency(balance));

            JOptionPane.showMessageDialog(this,
                    "Account Number: " + accNumber + "\nCurrent Balance: " + formatCurrency(balance),
                    "Balance Info",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    /** Handles the "Clear" button: resets all input fields and the balance display. */
    private void handleClear() {
        nameField.setText("");
        accNumberField.setText("");
        initialBalanceField.setText("");
        depositField.setText("");
        withdrawField.setText("");
        checkBalanceAccNumberField.setText("");
        balanceValueLabel.setText("Rs. 0.00");
        activeAccountNumber = null;
    }

    /** Handles the "Exit" button: confirms, then closes the application. */
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // =========================================================
    //  HELPER METHODS
    // =========================================================

    /** Throws a clear error if no account has been created/selected yet. */
    private void ensureActiveAccount() {
        if (activeAccountNumber == null || !bankManager.accountExists(activeAccountNumber)) {
            throw new IllegalArgumentException("Please create an account first.");
        }
    }

    /** Formats a double as an Indian Rupee currency string, e.g. "Rs. 1234.50". */
    private String formatCurrency(double amount) {
        return "Rs. " + String.format("%.2f", amount);
    }

    /** Shows a red error dialog using JOptionPane. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Creates a bold section header label (e.g. "Create Account"). */
    private JLabel sectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(TEXT_DARK);
        return label;
    }

    /** Creates a small muted label above an input field (e.g. "Account Number"). */
    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /**
     * Creates a JTextField styled to look modern: light gray fill and a
     * soft rounded-looking border (line border + padding). No manual
     * width is set here -- GridBagLayout's HORIZONTAL fill + weightx
     * (applied where each field is added) is what makes these fields
     * stretch to fill their column properly.
     */
    private JTextField styledTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_FIELD);
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_DARK);
        field.setCaretColor(ACCENT);
        field.setPreferredSize(new Dimension(100, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return field;
    }

    /**
     * Small helper that builds a GridBagConstraints object for a given
     * grid cell. Centralizing this avoids repeating 6 lines of
     * boilerplate for every single component added to the form.
     *
     * @param x       column index (LEFT_COL or RIGHT_COL)
     * @param y       row index
     * @param w       how many columns this component spans (1 or FULL_WIDTH)
     * @param fill    GridBagConstraints.HORIZONTAL (stretch to column width)
     *                or GridBagConstraints.NONE (keep natural/preferred size,
     *                used for buttons so they never look "stretched/oversized")
     * @param weightx how much extra horizontal space this cell claims
     *                relative to siblings in the same row
     * @param in      spacing (margin) around the component
     */
    private GridBagConstraints gbc(int x, int y, int w, int fill, double weightx, Insets in) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.fill = fill;
        c.weightx = weightx;
        c.insets = in;
        c.anchor = GridBagConstraints.CENTER;
        return c;
    }

    /** Shorthand for building an Insets object (top, left, bottom, right). */
    private Insets insets(int top, int left, int bottom, int right) {
        return new Insets(top, left, bottom, right);
    }

    // =========================================================
    //  CUSTOM COMPONENTS (rounded look, since plain Swing
    //  buttons/panels are square by default)
    // =========================================================

    /**
     * RoundedPanel: a JPanel subclass that paints itself as a
     * rounded rectangle instead of a plain square box. Used for
     * the main white "card" in the center of the window.
     */
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color bgColor;

        RoundedPanel(int cornerRadius, Color bgColor) {
            this.cornerRadius = cornerRadius;
            this.bgColor = bgColor;
            setOpaque(false); // we paint the background ourselves
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * RoundedButton: a JButton subclass that paints a solid rounded
     * rectangle background instead of default OS chrome, and
     * lightens/darkens slightly on hover. Sized to its text via
     * setPreferredSize, and placed with GridBagConstraints.NONE fill
     * so it is never stretched to fill its column (fixes the
     * "oversized buttons" problem from the previous BoxLayout version).
     */
    private static class RoundedButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private Color currentColor;

        RoundedButton(String text, Color baseColor, Color hoverColor, Color textColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.currentColor = baseColor;

            setFont(FONT_BUTTON);
            setForeground(textColor);
            setFocusPainted(false);        // removes the default focus rectangle
            setContentAreaFilled(false);   // we paint the background ourselves
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(190, 42));
            setBorder(new EmptyBorder(8, 22, 8, 22));

            // Swap colors on hover for a modern, tactile feel.
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
