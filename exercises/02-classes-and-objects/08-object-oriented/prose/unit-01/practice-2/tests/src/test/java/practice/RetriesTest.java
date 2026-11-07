package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import practice.Retries.Charge;
import practice.Retries.Idempotent;
import practice.Retries.Payment;
import practice.Retries.Refund;

import static org.assertj.core.api.Assertions.assertThat;

class RetriesTest {

    /** A bank that refuses the first {@code failures} attempts and accepts the rest; it counts every attempt. */
    static final class Bank implements BooleanSupplier {
        final AtomicInteger attempts = new AtomicInteger();
        private final int failures;

        Bank(int failures) { this.failures = failures; }

        @Override public boolean getAsBoolean() { return attempts.incrementAndGet() > failures; }
    }

    @BeforeEach
    void defaultPolicy() {
        Retries.idempotentAttempts = 4;
    }

    @Test
    void aChargeIsSentOnceWhetherOrNotTheBankAccepts() {
        Bank accepting = new Bank(0);
        Bank refusing = new Bank(1);

        assertThat(Retries.send(new Charge(12_50), accepting)).isTrue();
        assertThat(Retries.send(new Charge(12_50), refusing)).isFalse();

        assertThat(accepting.attempts).hasValue(1);
        assertThat(refusing.attempts).hasValue(1);
    }

    @Test
    void aRefundTheBankAcceptsAtOnceIsSentOnce() {
        Bank bank = new Bank(0);

        assertThat(Retries.send(new Refund(80_00, "ch_31"), bank)).isTrue();

        assertThat(bank.attempts).hasValue(1);
    }

    @Test
    void aRefundIsRetriedAsOftenAsTheIdempotencyPolicyAllows() {
        Bank flaky = new Bank(3);
        Bank down = new Bank(100);
        Payment refund = new Refund(5_00, "ch_77");

        assertThat(Retries.send(refund, flaky)).isTrue();
        assertThat(Retries.send(new Refund(5_00, "ch_78"), down)).isFalse();

        assertThat(flaky.attempts).hasValue(4);
        assertThat(down.attempts).hasValue(4);
        assertThat(((Idempotent) refund).maxAttempts()).isEqualTo(4);
    }

    @Test
    void aRefundFollowsThePolicyWhenThePlatformChangesIt() {
        Retries.idempotentAttempts = 6;
        Bank flaky = new Bank(5);
        assertThat(Retries.send(new Refund(19_99, "ch_204"), flaky)).isTrue();
        assertThat(flaky.attempts).hasValue(6);

        Retries.idempotentAttempts = 2;
        Bank down = new Bank(100);
        assertThat(Retries.send(new Refund(19_99, "ch_205"), down)).isFalse();
        assertThat(down.attempts).hasValue(2);
    }
}
