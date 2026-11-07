package practice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hands out the launch vouchers. Sign-ups arrive on many request threads at once. Vouchers are
 * numbered 1, 2, 3, ... in the order they are granted, and each one is emailed to its customer.
 */
public class LaunchVouchers {

    /** Decides whether one more voucher may go out, given how many have been granted so far. */
    public interface Quota {
        boolean allows(int grantedSoFar);
    }

    /** Sends the voucher email. Each call is one email in a customer's inbox. */
    public interface Mailer {
        void sendVoucher(String customer, int voucherNumber);
    }

    private final AtomicInteger granted = new AtomicInteger();
    private final Quota quota;
    private final Mailer mailer;

    public LaunchVouchers(Quota quota, Mailer mailer) {
        this.quota = quota;
        this.mailer = mailer;
    }

    /** Grants the customer the next voucher if the quota allows one more. True when they got one. */
    public boolean claim(String customer) {
        if (!quota.allows(granted.get())) return false;
        while (true) {
            int n = granted.get();
            if (granted.compareAndSet(n, n + 1)) {
                mailer.sendVoucher(customer, n + 1);
                return true;
            }
        }
    }

    /** How many vouchers have been granted. */
    public int granted() {
        return granted.get();
    }
}
