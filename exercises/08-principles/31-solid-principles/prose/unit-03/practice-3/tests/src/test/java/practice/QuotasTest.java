package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuotasTest {

    private static final List<String> SEVEN = List.of("m1", "m2", "m3", "m4", "m5", "m6", "m7");

    @Test
    void grantsWhatIsAskedWhileTheBudgetLasts() {
        Quotas.DailyQuota quota = new Quotas.DailyQuota(40);

        assertThat(quota.grant(12)).isEqualTo(12);
        assertThat(quota.grant(25)).isEqualTo(25);
        assertThat(quota.remaining()).isEqualTo(3);
    }

    @Test
    void aBatchBiggerThanWhatIsLeftSendsWhatIsLeft() {
        Quotas.DailyQuota quota = new Quotas.DailyQuota(9);
        quota.grant(4);

        assertThat(Quotas.sendable(SEVEN, quota)).containsExactly("m1", "m2", "m3", "m4", "m5");
    }

    @Test
    void anEmptyBatchGoesThroughLikeAnyOther() {
        Quotas.DailyQuota quota = new Quotas.DailyQuota(6);

        assertThat(Quotas.sendable(List.of(), quota)).isEmpty();
        assertThat(quota.remaining()).isEqualTo(6);
    }

    @Test
    void anExhaustedQuotaKeepsGrantingNothing() {
        Quotas.DailyQuota quota = new Quotas.DailyQuota(5);
        quota.grant(3);
        quota.grant(30);

        assertThat(quota.grant(2)).isZero();
        assertThat(Quotas.sendable(SEVEN, quota)).isEmpty();
    }
}
