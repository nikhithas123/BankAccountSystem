import java.util.HashMap;
import java.util.Map;

/**
 * BankAccountManager.java
 * ---------------------------------------------------------
 * SERVICE / BUSINESS-LOGIC CLASS
 *
 * This class manages MULTIPLE BankAccount objects at once.
 * Think of it as the "bank" itself, while BankAccount is a
 * single "account" inside that bank.
 *
 * Data storage requirement: "Store data in memory only" ->
 * We use a HashMap<String, BankAccount> where the key is the
 * account number (String) and the value is the BankAccount
 * object. A HashMap gives us fast lookup by account number
 * (O(1) average time), which is why it's the right data
 * structure here instead of, say, an ArrayList.
 *
 * This class is completely independent of Swing. It could be
 * reused in a console application or tested with JUnit with
 * zero changes. That is good OOP design (separation of
 * concerns / single responsibility principle).
 * ---------------------------------------------------------
 */
public class BankAccountManager {

    // In-memory storage: accountNumber -> BankAccount
    private Map<String, BankAccount> accounts;

    /**
     * Constructor: initializes the empty in-memory "database" (a HashMap).
     */
    public BankAccountManager() {
        accounts = new HashMap<>();
    }

    /**
     * Creates a new account and stores it in memory.
     *
     * Validation performed here (manager-level rules, as opposed to
     * single-account rules which live inside BankAccount):
     *   - Name must not be empty.
     *   - Account number must not be empty.
     *   - Account number must not already exist (no duplicates).
     *   - Initial balance must not be negative.
     */
    public void createAccount(String name, String accountNumber, double initialBalance) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }
        if (accounts.containsKey(accountNumber)) {
            throw new IllegalArgumentException("An account with this number already exists.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }

        BankAccount newAccount = new BankAccount(name, accountNumber, initialBalance);
        accounts.put(accountNumber, newAccount);
    }

    /**
     * Deposits money into the account identified by accountNumber.
     * Looks the account up, then delegates the actual deposit logic
     * to BankAccount.deposit() (which has its own validation).
     */
    public void deposit(String accountNumber, double amount) {
        BankAccount account = getAccountOrThrow(accountNumber);
        account.deposit(amount);
    }

    /**
     * Withdraws money from the account identified by accountNumber.
     */
    public void withdraw(String accountNumber, double amount) {
        BankAccount account = getAccountOrThrow(accountNumber);
        account.withdraw(amount);
    }

    /**
     * Returns the current balance for the given account number.
     */
    public double getBalance(String accountNumber) {
        BankAccount account = getAccountOrThrow(accountNumber);
        return account.getBalance();
    }

    /**
     * Checks whether an account with the given number exists.
     */
    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    /**
     * Private helper method: looks up an account by number.
     * If it doesn't exist, throws a clear exception.
     *
     * This avoids repeating the same "if not found" check in
     * deposit(), withdraw(), and getBalance() -- a good example
     * of the DRY principle (Don't Repeat Yourself).
     */
    private BankAccount getAccountOrThrow(String accountNumber) {
        BankAccount account = accounts.get(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("No account found with number: " + accountNumber);
        }
        return account;
    }
}
