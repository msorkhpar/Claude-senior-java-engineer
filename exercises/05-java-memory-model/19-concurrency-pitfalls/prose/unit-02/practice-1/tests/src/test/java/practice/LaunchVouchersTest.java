package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class LaunchVouchersTest {

    /**
     * Allows a voucher while fewer than {@code limit} are granted. The very first check it is
     * asked for is slow: it waits until the test releases it, as a call to the campaign service
     * might.
     */
    static final class SlowFirstQuota implements LaunchVouchers.Quota {
        final int limit;
        final CountDownLatch checking = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean first = new AtomicBoolean(true);

        SlowFirstQuota(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean allows(int grantedSoFar) {
            if (first.compareAndSet(true, false)) {
                checking.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return grantedSoFar < limit;
        }
    }

    static final class Inbox implements LaunchVouchers.Mailer {
        final List<String> sent = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void sendVoucher(String customer, int voucherNumber) {
            sent.add(customer + "#" + voucherNumber);
        }
    }

    private final ExecutorService threads = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopThreads() {
        threads.shutdownNow();
    }

    /** Outcome of two sign-ups that meet: priya's quota check is slow, omar signs up meanwhile. */
    record Meeting(boolean priya, boolean omar, int granted, List<String> sent) {}

    private Meeting meet(int limit) throws Exception {
        SlowFirstQuota quota = new SlowFirstQuota(limit);
        Inbox inbox = new Inbox();
        LaunchVouchers vouchers = new LaunchVouchers(quota, inbox);

        Future<Boolean> priya = threads.submit(() -> vouchers.claim("priya"));
        assertThat(quota.checking.await(5, TimeUnit.SECONDS)).as("priya's claim asked the quota").isTrue();
        Future<Boolean> omar = threads.submit(() -> vouchers.claim("omar"));
        boolean omarGot = omar.get(5, TimeUnit.SECONDS);   // must not wait for priya's slow check
        quota.release.countDown();
        boolean priyaGot = priya.get(5, TimeUnit.SECONDS);
        return new Meeting(priyaGot, omarGot, vouchers.granted(), List.copyOf(inbox.sent));
    }

    @Test
    void grantsVouchersInOrderUntilTheQuotaIsReached() {
        Inbox inbox = new Inbox();
        LaunchVouchers vouchers = new LaunchVouchers(granted -> granted < 3, inbox);

        List<Boolean> results = new ArrayList<>();
        for (String customer : List.of("noor", "li", "sam", "ade", "kim")) results.add(vouchers.claim(customer));

        assertThat(results).containsExactly(true, true, true, false, false);
        assertThat(inbox.sent).containsExactly("noor#1", "li#2", "sam#3");
        assertThat(vouchers.granted()).isEqualTo(3);
    }

    @Test
    void aCustomerWhoseSignUpMetAnotherIsEmailedOnce() throws Exception {
        Meeting m = meet(5);

        assertThat(m.sent()).hasSize(2);
        assertThat(m.sent()).filteredOn(s -> s.startsWith("priya#")).hasSize(1);
        assertThat(m.sent()).filteredOn(s -> s.startsWith("omar#")).hasSize(1);
    }

    @Test
    void twoSignUpsThatMeetGetTwoDifferentVouchers() throws Exception {
        Meeting m = meet(5);

        assertThat(m.granted()).isEqualTo(2);
        assertThat(m.sent()).extracting(s -> s.substring(s.indexOf('#') + 1)).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void aSignUpThatMeetsAnotherStillGetsAVoucherWhileTheQuotaAllows() throws Exception {
        Meeting m = meet(5);

        assertThat(m.omar()).isTrue();
        assertThat(m.priya()).isTrue();
    }

    @Test
    void theLastVoucherGoesToOnlyOneOfTwoSignUpsThatMeet() throws Exception {
        Meeting m = meet(1);

        assertThat(m.omar()).isTrue();
        assertThat(m.priya()).isFalse();
        assertThat(m.granted()).isEqualTo(1);
        assertThat(m.sent()).containsExactly("omar#1");
    }
}
