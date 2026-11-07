package practice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRatesTest {

    /** A treasury whose rates the test changes, counting the calls it gets. */
    static final class FakeTreasury implements ExchangeRates.Treasury {
        final Map<String, BigDecimal> quoted = new HashMap<>(Map.of(
                "USD", new BigDecimal("1.0842"),
                "GBP", new BigDecimal("0.8571"),
                "JPY", new BigDecimal("162.37")));
        int calls;

        @Override
        public Map<String, BigDecimal> allRates() {
            calls++;
            return new HashMap<>(quoted);
        }
    }

    /** A clock the test moves by hand. */
    static final class HandClock implements InstantSource {
        Instant now = Instant.parse("2026-05-11T14:00:00Z");

        @Override
        public Instant instant() {
            return now;
        }

        void minutes(long m) {
            now = now.plus(Duration.ofMinutes(m));
        }
    }

    private final FakeTreasury treasury = new FakeTreasury();
    private final HandClock clock = new HandClock();
    private final ExchangeRates rates = new ExchangeRates(treasury, clock);

    @Test
    void convertsAtTheTreasurysRateForEachCurrency() {
        assertThat(rates.rate("USD")).isEqualByComparingTo("1.0842");
        clock.minutes(4);
        assertThat(rates.rate("JPY")).isEqualByComparingTo("162.37");
        assertThat(rates.rate("GBP")).isEqualByComparingTo("0.8571");
        clock.minutes(5);
        assertThat(rates.rate("USD")).isEqualByComparingTo("1.0842");
    }

    @Test
    void aRateReadEveryFewMinutesStillChangesWithinTheLimit() {
        rates.rate("USD");
        clock.minutes(1);
        treasury.quoted.put("USD", new BigDecimal("1.0917"));
        for (int minute = 3; minute <= 30; minute += 3) {
            clock.minutes(minute == 3 ? 2 : 3);
            BigDecimal used = rates.rate("USD");
            if (minute > 11) {
                assertThat(used).as("rate used at minute %d, %d minutes after the treasury changed it", minute, minute - 1)
                        .isEqualByComparingTo("1.0917");
            }
        }
    }

    @Test
    void aRateChangedWhileNobodyPaidIsUsedAtTheNextPayment() {
        rates.rate("GBP");
        treasury.quoted.put("GBP", new BigDecimal("0.8604"));
        clock.minutes(25);

        assertThat(rates.rate("GBP")).isEqualByComparingTo("0.8604");
    }

    @Test
    void paymentsWithinTheLimitCallTheTreasuryOnce() {
        for (int minute = 0; minute < 10; minute += 2) {
            rates.rate("JPY");
            clock.minutes(2);
        }

        assertThat(treasury.calls).isEqualTo(1);
    }

    @Test
    void paymentsInSeveralCurrenciesShareOneCallPerPeriod() {
        rates.rate("USD");
        rates.rate("GBP");
        rates.rate("JPY");
        clock.minutes(11);
        rates.rate("GBP");
        rates.rate("JPY");
        rates.rate("USD");

        assertThat(treasury.calls).isEqualTo(2);
    }
}
