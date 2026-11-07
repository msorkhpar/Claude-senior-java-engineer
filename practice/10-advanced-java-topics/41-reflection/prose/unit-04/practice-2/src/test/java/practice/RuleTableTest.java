package practice;

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleTableTest {

    static class PercentOff {
        private final int percent;

        PercentOff(int percent) {
            this.percent = percent;
        }

        public int apply(int cents) {
            return cents - cents * percent / 100;
        }
    }

    static class FlatFee {
        private final int fee;

        FlatFee(int fee) {
            this.fee = fee;
        }

        public int apply(int cents) {
            return cents + fee;
        }
    }

    static class NoRule {
        public long apply(long cents) {
            return cents;
        }
    }

    @Test
    void appliesEachRuleByName() {
        RuleTable table = new RuleTable();
        table.add("spring-sale", new PercentOff(15));
        table.add("handling", new FlatFee(250));

        assertThat(table.apply("spring-sale", 4_000)).isEqualTo(3_400);
        assertThat(table.apply("handling", 4_000)).isEqualTo(4_250);
        assertThat(table.apply("spring-sale", 13_999)).isEqualTo(11_900);
    }

    @Test
    void anObjectWithoutTheRuleMethodIsRefusedWhenAdded() {
        RuleTable table = new RuleTable();

        assertThatThrownBy(() -> table.add("typo", new NoRule()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoRulesOfOneClassKeepTheirOwnSettings() {
        RuleTable table = new RuleTable();
        table.add("members", new PercentOff(10));
        table.add("clearance", new PercentOff(30));

        assertThat(table.apply("members", 8_000)).isEqualTo(7_200);
        assertThat(table.apply("clearance", 8_000)).isEqualTo(5_600);
        assertThat(table.apply("members", 2_500)).isEqualTo(2_250);
    }

    @Test
    void applyingARuleAllocatesNothing() {
        RuleTable table = new RuleTable();
        table.add("loyalty", new PercentOff(20));
        table.add("gift-wrap", new FlatFee(399));
        var threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().threadId();

        long sum = 0;
        for (int i = 0; i < 50_000; i++) {           // first calls link the call sites
            sum += table.apply("loyalty", 10_000 + i) + table.apply("gift-wrap", 10_000 + i);
        }
        int calls = 400_000;
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < calls / 2; i++) {
            sum += table.apply("loyalty", 20_000 + i) + table.apply("gift-wrap", 20_000 + i);
        }
        long allocated = threads.getThreadAllocatedBytes(thread) - before;

        assertThat(sum).isPositive();
        assertThat(allocated).as("bytes allocated by %d calls", calls).isLessThan(calls);
    }
}
