package practice;

import java.util.Objects;
import java.util.function.Predicate;

/** Three account forms that each check an email address. */
public class AccountForms {

    private final Predicate<String> emailRule;

    /** Forms that check addresses with the default rule, isValidEmail. */
    public AccountForms() {
        this(AccountForms::isValidEmail);
    }

    /** Forms that check addresses with the given rule. */
    public AccountForms(Predicate<String> emailRule) {
        this.emailRule = Objects.requireNonNull(emailRule, "emailRule must not be null");
    }

    /** The default rule: not null, not blank, and holding both "@" and ".". */
    public static boolean isValidEmail(String email) {
        throw new UnsupportedOperationException("write isValidEmail");
    }

    /** Whether the registration form accepts this address. */
    public boolean register(String email) {
        throw new UnsupportedOperationException("write register");
    }

    /** Whether the password-reset form accepts this address. */
    public boolean resetPassword(String email) {
        throw new UnsupportedOperationException("write resetPassword");
    }

    /** Whether the change-email form accepts this address. */
    public boolean changeEmail(String email) {
        throw new UnsupportedOperationException("write changeEmail");
    }
}
