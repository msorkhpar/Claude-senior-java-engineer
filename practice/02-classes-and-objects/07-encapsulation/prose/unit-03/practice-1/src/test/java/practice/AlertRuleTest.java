package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertRuleTest {

    @Test
    void aValidRuleIsInTheBookTheEvaluatorWalks() {
        AlertRule.RuleBook book = new AlertRule.RuleBook();
        AlertRule disk = new AlertRule(book, "disk.used.percent", 91.5, Duration.ofMinutes(15));
        AlertRule queue = new AlertRule(book, "orders.queue.depth", 2_500, Duration.ofSeconds(90));

        assertThat(book.rules()).containsExactly(disk, queue);
        assertThat(disk.metric()).isEqualTo("disk.used.percent");
        assertThat(disk.threshold()).isEqualTo(91.5);
        assertThat(queue.window()).isEqualTo(Duration.ofSeconds(90));
        assertThat(book.samplesNeeded()).containsExactly(15L, 1L);
    }

    @Test
    void refusesRulesTheEvaluatorCannotRun() {
        AlertRule.RuleBook book = new AlertRule.RuleBook();
        assertThatThrownBy(() -> new AlertRule(book, "  ", 10, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, null, 10, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, "cpu.load", Double.NaN, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, "cpu.load", Double.POSITIVE_INFINITY, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, "cpu.load", 4, Duration.ofSeconds(59)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, "cpu.load", 4, Duration.ofMinutes(-3)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlertRule(book, "cpu.load", 4, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRefusedRuleNeverReachesTheEvaluator() {
        AlertRule.RuleBook book = new AlertRule.RuleBook();
        AlertRule kept = new AlertRule(book, "http.5xx.rate", 0.02, Duration.ofMinutes(10));
        for (Duration typo : new Duration[] {null, Duration.ZERO, Duration.ofSeconds(30)}) {
            try {
                new AlertRule(book, "http.p99.latency", 750, typo);
            } catch (IllegalArgumentException refused) {
                // the API answers 400 and moves on
            }
        }

        assertThat(book.rules()).containsExactly(kept);
        assertThat(book.samplesNeeded()).containsExactly(10L);
    }
}
