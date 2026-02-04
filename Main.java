import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

abstract class Account {
    private final String holderName;
    private final String accountNumber;
    private final String pinHash;

    // Data integrity: store money in paise (no double)
    private long balancePaise;

    // Security
    private int wrongAttempts = 0;
    private boolean locked = false;

    // Mini statement
    private final List<String> transactions = new ArrayList<>();

    public Account(String holderName, String accountNumber, String pinPlainText, long openingBalancePaise) {
        this.holderName = holderName;
        this.accountNumber = accountNumber;
        this.pinHash = SecurityUtil.sha256(pinPlainText);
        this.balancePaise = openingBalancePaise;

        transactions.add("Account Created | Opening Balance: " + Money.format(balancePaise));
    }

    public String getHolderName() {
        return holderName;
    }

    public String getAccountNumber() {
        return accountNumber;
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

    protected void setBalancePaise(long newBalance) {
        this.balancePaise = newBalance;
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

    public void showBalance() {
        System.out.println("Balance: " + Money.format(balancePaise));
    }

    public void deposit(long amountPaise) {
        if (locked) {
            System.out.println("Account is locked.");
            return;
        }

        if (amountPaise <= 0) {
            System.out.println("Deposit must be greater than 0.");
            return;
        }

        balancePaise += amountPaise;
        transactions.add("Deposit: " + Money.format(amountPaise) + " | Balance: " + Money.format(balancePaise));
        System.out.println("Deposit successful. Balance: " + Money.format(balancePaise));
    }

    public void withdraw(long amountPaise) {
        if (locked) {
            System.out.println("Account is locked.");
            return;
        }

        if (amountPaise <= 0) {
            System.out.println("Withdraw must be greater than 0.");
            return;
        }

        if (amountPaise > balancePaise) {
            System.out.println("Insufficient balance.");
            return;
        }

        balancePaise -= amountPaise;
        transactions.add("Withdraw: " + Money.format(amountPaise) + " | Balance: " + Money.format(balancePaise));
        System.out.println("Withdraw successful. Balance: " + Money.format(balancePaise));
    }

    public void showMiniStatement() {
        System.out.println("\n--- Mini Statement (Last 5) ---");
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            System.out.println((i + 1) + ". " + transactions.get(i));
        }
    }

    public abstract void accountType();
}

class SavingsAccount extends Account {
    public SavingsAccount(String holderName, String accountNumber, String pin, long openingBalancePaise) {
        super(holderName, accountNumber, pin, openingBalancePaise);
    }

    @Override
    public void accountType() {
        System.out.println("Account Type: Savings Account");
    }
}

class Money {
    public static String format(long paise) {
        long rupees = paise / 100;
        long decimals = Math.abs(paise % 100);
        return "Rs " + rupees + "." + (decimals < 10 ? "0" + decimals : decimals);
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

class AccountFactory {
    public static String generateAccountNumber() {
        Random r = new Random();
        int num = 10000000 + r.nextInt(90000000);
        return "SB" + num;
    }
}

class ATM {
    private final Account account;
    private final Scanner sc;

    public ATM(Account account, Scanner sc) {
        this.account = account;
        this.sc = sc;
    }

    public void loginAndRun() {
        System.out.println("\n===== ATM LOGIN =====");
        System.out.print("Enter Account Holder Name: ");
        String name = sc.nextLine().trim();

        if (!name.equalsIgnoreCase(account.getHolderName())) {
            System.out.println("Account holder not found!");
            return;
        }

        // Auto-fill/show account number
        System.out.println("Account Number: " + account.getAccountNumber());

        while (true) {
            if (account.isLocked()) {
                System.out.println("Account LOCKED due to 3 wrong PIN attempts.");
                return;
            }

            System.out.print("Enter PIN: ");
            String enteredPin = sc.nextLine().trim();

            if (account.verifyPin(enteredPin)) {
                System.out.println("Login successful!");
                break;
            } else {
                System.out.println("Wrong PIN. Attempts left: " + account.getRemainingAttempts());
            }
        }

        // Menu loop
        while (true) {
            System.out.println("\n===== ATM MENU =====");
            System.out.println("1) Check Balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Mini Statement");
            System.out.println("5) Logout");

            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> account.showBalance();

                case "2" -> {
                    System.out.print("Deposit Amount (Rs): ");
                    long amt = Long.parseLong(sc.nextLine().trim());
                    account.deposit(amt * 100);
                }

                case "3" -> {
                    System.out.print("Withdraw Amount (Rs): ");
                    long amt = Long.parseLong(sc.nextLine().trim());
                    account.withdraw(amt * 100);
                }

                case "4" -> account.showMiniStatement();

                case "5" -> {
                    System.out.println("Logged out.");
                    return;
                }

                default -> System.out.println("Invalid option.");
            }
        }
    }
}

public class Main {

    private static String createPin(Scanner sc) {
        while (true) {
            System.out.print("Create 4-digit PIN: ");
            String pin1 = sc.nextLine().trim();

            System.out.print("Confirm PIN: ");
            String pin2 = sc.nextLine().trim();

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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== CREATE ACCOUNT =====");
        System.out.print("Enter Account Holder Name: ");
        String name = sc.nextLine().trim();

        String accountNo = AccountFactory.generateAccountNumber();

        System.out.println("\nYour generated Account Number: " + accountNo);
        System.out.println("Now set your ATM PIN (PIN will not be displayed later).");

        String pin = createPin(sc);

        Account account = new SavingsAccount(name, accountNo, pin, 5000L * 100);

        System.out.println("\n✅ Account Created Successfully!");
        System.out.println("Holder Name : " + account.getHolderName());
        System.out.println("Account No  : " + account.getAccountNumber());
        System.out.println("(PIN saved securely and not displayed)");

        ATM atm = new ATM(account, sc);
        atm.loginAndRun();

        sc.close();
    }
}
