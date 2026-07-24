/**
 * BankAccount.java
 * ---------------------------------------------------------
 * MODEL CLASS (OOP Principle: Encapsulation)
 *
 * This class represents a single bank account in memory.
 * It stores the account's data as PRIVATE fields, and the
 * only way the outside world can read or change that data
 * is through public methods (getters and deposit/withdraw).
 *
 * This is encapsulation: the internal state (balance) is
 * protected from being changed directly/incorrectly from
 * outside the class. For example, nobody can do
 * "account.balance = -500;" because "balance" is private.
 * They MUST go through withdraw()/deposit(), which contain
 * validation rules.
 * ---------------------------------------------------------
 */
public class BankAccount {

    // ----------------- FIELDS (private = encapsulated) -----------------
    private String accountHolderName;
    private String accountNumber;
    private double balance;

    /**
     * Constructor: runs when a new BankAccount object is created.
     * It initializes the three fields with values passed in.
     */
    public BankAccount(String accountHolderName, String accountNumber, double initialBalance) {
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    // ----------------- GETTERS (read-only access to private data) -----------------

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    // ----------------- CORE BUSINESS METHODS -----------------

    /**
     * Deposit money into this account.
     * Validation rule: amount must be strictly greater than zero.
     *
     * We THROW an exception instead of just printing an error,
     * because this is the Model layer -- it should not know about
     * GUI dialogs. The GUI layer (BankManagementGUI) will catch
     * this exception and show a JOptionPane message.
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }
        balance += amount; // increase balance
    }

    /**
     * Withdraw money from this account.
     * Validation rules:
     *   1. Amount must be greater than zero.
     *   2. Amount must not exceed the current balance (no overdraft).
     */
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance. Available balance: Rs. "
                    + String.format("%.2f", balance));
        }
        balance -= amount; // decrease balance
    }

    /**
     * Returns a neat text summary of the account.
     * Useful for debugging or displaying details.
     */
    @Override
    public String toString() {
        return "Account[Holder=" + accountHolderName +
                ", Number=" + accountNumber +
                ", Balance=" + String.format("%.2f", balance) + "]";
    }
}
