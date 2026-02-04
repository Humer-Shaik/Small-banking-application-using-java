import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.io.Console;

abstract class Account {
    private final String holderName;
    private final String accountNumber;

    // store PIN as hash
    private final String pinHash;

    private long balancePaise;

    private int wrongAttempts = 0;
    private boolean locked = false;

    private final List<String> transactions = new ArrayList<>();

    public Account(String holderName, String accountNumber, String pinPlainText, long openingBalancePaise) {
        this.holderName = holderName;
        this.accountNumber = accountNumber;
        this.pinHash = SecurityUtil.sha256(pinPlainText);
        this.balancePaise = openingBalancePaise;

        transactions.add("Account created with opening balance: " + Money.format(balancePaise));
    }

    public String getHolderName() {
        return holderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean verifyPin(String enteredPin) {
        if (locked) return false;

        String enteredHash = SecurityUtil.sha256(enteredPin);
        if (pinHash.equals(enteredHash)) {
            wrongAttempts = 0;
            return true;
        } else {
            wrongAttempts++;
            if (wrongAttempts >= 3) locked = true;
            return false;
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public int getRemainingAttempts() {
        return Math.max(0, 3 - wrongAttempts);
    }

    public long getBalancePaise() {
        return balancePaise;
    }

    protected void setBalancePaise(long newBalancePaise) {
        this.balancePaise = newBalancePaise;
    }

    public final void deposit(long amountPaise) {
        if (locked) {
            System.out.println("Account is locked.");
            return;
        }

        if (amountPaise <= 0) {
            System.out.println("Deposit must be greater than 0.");
            return;
        }

        balancePaise += amountPaise;
        transactions.add("Deposited: " + Money.format(amountPaise) + " | Balance: " + Money.format(balancePaise));
        System.out.println("Deposit successful. Balance: " + Money.format(balancePaise));
    }

    public void withdraw(long amountPaise) {
        if (locked) {
            System.out.println("Account is locked.");
            return;
        }

        if (amountPaise <= 0) {
            System.out.println("Withdrawal must be greater than 0.");
            return;
        }

        if (amountPaise > balancePaise) {
            System.out.println("Not enough balance.");
            return;
        }

        balancePaise -= amountPaise;
        transactions.add("Withdrawn: " + Money.format(amountPaise) + " | Balance: " + Money.format(balancePaise));
        System.out.println("Withdrawal successful. Balance: " + Money.format(balancePaise));
    }

    public void showBalance() {
        System.out.println("Balance: " + Money.format(balancePaise));
    }

    public void showMiniStatement() {
        System.out.println("\n--- Mini Statement ---");
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            System.out.println((i + 1) + ". " + transactions.get(i));
        }
    }

    public abstract void accountType();
}

class SavingsAccount extends Account {
    public SavingsAccount(String holderName, String accountNumber, String pin, long balancePaise) {
        super(holderName, accountNumber, pin, balancePaise);
    }

    @Override
    public void accountType() {
        System.out.println("Account: Savings");
    }
}

class CurrentAccount extends Account {
    private final long overdraftLimitPaise;

    public CurrentAccount(String holderName, String accountNumber, String pin, long balancePaise, long overdraftLimitPaise) {
        super(holderName, accountNumber, pin, balancePaise);
        this.overdraftLimitPaise = overdraftLimitPaise;
    }

    @Override
    public void withdraw(long amountPaise) {
        if (isLocked()) {
            System.out.println("Account is locked.");
            return;
        }

        if (amountPaise <= 0) {
            System.out.println("Withdrawal must be greater than 0.");
            return;
        }

        long allowed = getBalancePaise() + overdraftLimitPaise;
        if (amountPaise > allowed) {
            System.out.println("Overdraft limit exceeded.");
            return;
        }

        setBalancePaise(getBalancePaise() - amountPaise);
        System.out.println("Withdrawal successful. Balance: " + Money.format(getBalancePaise()));
    }

    @Override
    public void accountType() {
        System.out.println("Account: Current (Overdraft enabled)");
    }
}

class ATM {
    private final Account userAccount;

    public ATM(Account userAccount) {
        this.userAccount = userAccount;
    }

    public void start() {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== ATM LOGIN =====");
        System.out.println("Account Number: " + userAccount.getAccountNumber());

        while (true) {
            if (userAccount.isLocked()) {
                System.out.println("Account locked due to 3 wrong PIN attempts.");
                sc.close();
                return;
            }

            String enteredPin = InputUtil.readHiddenPin("Enter PIN: ", sc);

            if (userAccount.verifyPin(enteredPin)) {
                break;
            }

            System.out.println("Wrong PIN. Attempts left: " + userAccount.getRemainingAttempts());
        }

        System.out.println("\nLogin successful.");
        System.out.println("Hi, " + userAccount.getHolderName());
        userAccount.accountType();

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1) Balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Mini Statement");
            System.out.println("5) Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> userAccount.showBalance();

                case 2 -> {
                    System.out.print("Amount to deposit (LKR): ");
                    long amt = sc.nextLong();
                    userAccount.deposit(amt * 100);
                }

                case 3 -> {
                    System.out.print("Amount to withdraw (LKR): ");
                    long amt = sc.nextLong();
                    userAccount.withdraw(amt * 100);
                }

                case 4 -> userAccount.showMiniStatement();

                case 5 -> {
                    System.out.println("Thanks. Session ended.");
                    sc.close();
                    return;
                }

                default -> System.out.println("Invalid option.");
            }
        }
    }
}

class Money {
    public static String format(long paise) {
        long rupees = paise / 100;
        long decimals = Math.abs(paise % 100);
        return "RS " + rupees + "." + (decimals < 10 ? "0" + decimals : decimals);
    }
}

class SecurityUtil {
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}

class InputUtil {

    // If console available -> hides input
    // If not -> fallback to scanner (visible input)
    public static String readHiddenPin(String prompt, Scanner sc) {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(prompt);
            return new String(password);
        } else {
            System.out.print(prompt);
            return sc.next();
        }
    }

    public static String createPin(Scanner sc) {
        while (true) {
            String pin1 = readHiddenPin("Create 4-digit PIN: ", sc);
            String pin2 = readHiddenPin("Confirm PIN: ", sc);

            if (!pin1.matches("\\d{4}")) {
                System.out.println("PIN must be exactly 4 digits.");
                continue;
            }

            if (!pin1.equals(pin2)) {
                System.out.println("PIN mismatch. Try again.");
                continue;
            }

            return pin1;
        }
    }
}

public class Main {

    private static String generateAccountNumber() {
        Random r = new Random();
        int num = 10000000 + r.nextInt(90000000);
        return "SB" + num;
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("===== CREATE NEW ACCOUNT =====");
        System.out.print("Enter Holder Name: ");
        sc.nextLine(); // safe
        String name = sc.nextLine();

        String accNo = generateAccountNumber();

        // user creates their own PIN (never printed)
        String pin = InputUtil.createPin(sc);

        Account account = new SavingsAccount(name, accNo, pin, 5000L * 100);

        System.out.println("\n===== ACCOUNT CREATED SUCCESSFULLY =====");
        System.out.println("Holder Name : " + account.getHolderName());
        System.out.println("Account No  : " + account.getAccountNumber());
        System.out.println("(PIN saved securely and not displayed)");
        System.out.println("========================================");

        ATM atm = new ATM(account);
        atm.start();
    }
}
