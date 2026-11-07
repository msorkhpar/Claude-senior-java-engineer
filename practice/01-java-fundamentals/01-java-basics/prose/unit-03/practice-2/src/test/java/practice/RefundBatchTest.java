package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundBatchTest {

    private final RefundBatch batch = new RefundBatch();

    private static RefundBatch.FeedLine line(String orderId, double euros) {
        return new RefundBatch.FeedLine(orderId, euros);
    }

    @Test
    void paysRoundAmountsInCents() {
        assertThat(batch.payouts(List.of(line("ord-310", 12.5), line("ord-311", 0.25), line("ord-312", 3.0))))
                .containsExactly(new RefundBatch.Payout("ord-310", 1250), new RefundBatch.Payout("ord-311", 25),
                        new RefundBatch.Payout("ord-312", 300));
    }

    @Test
    void anAmountWithCentsIsPaidInFull() {
        assertThat(batch.payouts(List.of(line("ord-401", 1.15), line("ord-402", 8.2), line("ord-403", 19.99))))
                .containsExactly(new RefundBatch.Payout("ord-401", 115), new RefundBatch.Payout("ord-402", 820),
                        new RefundBatch.Payout("ord-403", 1999));
    }

    @Test
    void aReversalWithCentsTakesBackTheFullAmount() {
        assertThat(batch.payouts(List.of(line("ord-501", -1.15), line("ord-502", -0.29))))
                .containsExactly(new RefundBatch.Payout("ord-501", -115), new RefundBatch.Payout("ord-502", -29));
    }

    @Test
    void aLineWithoutAUsableAmountStopsTheBatch() {
        assertThatThrownBy(() -> batch.payouts(List.of(line("ord-601", 4.0), line("ord-602", Double.NaN))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> batch.payouts(List.of(line("ord-603", Double.POSITIVE_INFINITY))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
