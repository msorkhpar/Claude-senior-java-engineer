package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OverridesTest {

    static final class InvoiceRun {
        static final Clock CLOCK = Clock.systemUTC();
        static String region = "eu-west";
        private String currency = "EUR";
        private final Duration timeout;
        private int batchSize = 200;

        InvoiceRun(Duration timeout) {
            this.timeout = timeout;
        }

        String currency() { return currency; }
        Duration timeout() { return timeout; }
        int batchSize() { return batchSize; }
    }

    record Tariff(String zone, long centsPerKg) {}

    private static Map<String, Object> inOrder(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], pairs[i + 1]);
        return map;
    }

    @BeforeEach
    void resetStatics() {
        InvoiceRun.region = "eu-west";
    }

    @Test
    void replacesPrivateAndStaticFieldsThatAreNotFinal() {
        InvoiceRun run = new InvoiceRun(Duration.ofSeconds(30));

        Overrides.apply(run, inOrder("currency", "CHF", "batchSize", 5, "region", "ap-south"));

        assertThat(run.currency()).isEqualTo("CHF");
        assertThat(run.batchSize()).isEqualTo(5);
        assertThat(InvoiceRun.region).isEqualTo("ap-south");
    }

    @Test
    void aStaticFinalFieldIsRefusedBeforeAnythingIsWritten() {
        InvoiceRun run = new InvoiceRun(Duration.ofSeconds(30));
        Clock frozen = Clock.fixed(Instant.parse("2025-11-30T23:59:00Z"), ZoneOffset.UTC);

        assertThatThrownBy(() -> Overrides.apply(run, inOrder("currency", "GBP", "CLOCK", frozen)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CLOCK");
        assertThat(run.currency()).isEqualTo("EUR");
    }

    @Test
    void aRecordComponentIsRefused() {
        Tariff tariff = new Tariff("Z4", 1180);

        assertThatThrownBy(() -> Overrides.apply(tariff, inOrder("centsPerKg", 990L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("centsPerKg");
        assertThat(tariff.centsPerKg()).isEqualTo(1180);
    }

    @Test
    void aFinalInstanceFieldOfAnOrdinaryClassIsReplaced() {
        InvoiceRun run = new InvoiceRun(Duration.ofSeconds(30));

        Overrides.apply(run, inOrder("timeout", Duration.ofMillis(250), "batchSize", 1));

        assertThat(run.timeout()).isEqualTo(Duration.ofMillis(250));
        assertThat(run.batchSize()).isEqualTo(1);
    }
}
