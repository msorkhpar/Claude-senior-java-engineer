package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Decides where an invoice for an account is e-mailed. */
public class BillingContacts {

    /** Where invoices go when an account has no billing address of its own. */
    public static final String RECEIVABLES = "receivables@example.com";

    /** Thrown when an invoice names an account that doesn't exist. */
    public static class UnknownAccountException extends RuntimeException {
        public UnknownAccountException(String message) {
            super(message);
        }
    }

    /** An account; its billing e-mail is optional. */
    public static final class Account {
        private final String id;
        private final String billingEmail;   // null when the customer gave none

        public Account(String id, String billingEmail) {
            this.id = id;
            this.billingEmail = billingEmail;
        }

        public String id() {
            return id;
        }

        public Optional<String> billingEmail() {
            return Optional.ofNullable(billingEmail);
        }
    }

    private final Map<String, Account> accounts = new HashMap<>();

    public void add(Account account) {
        accounts.put(account.id(), account);
    }

    public Optional<Account> find(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    /** The address the invoice for this account is sent to. */
    public String invoiceAddress(String accountId) {
        return find(accountId)
                .orElseThrow(() -> new UnknownAccountException("no account " + accountId))
                .billingEmail()
                .get();
    }
}
