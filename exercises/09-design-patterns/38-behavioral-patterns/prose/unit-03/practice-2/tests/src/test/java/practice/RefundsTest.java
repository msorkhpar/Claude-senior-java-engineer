package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class RefundsTest {

    private static final Duration LIMIT = Duration.ofSeconds(5);

    private final Refunds.RefundService service = new Refunds.RefundService();
    private final AtomicInteger sends = new AtomicInteger();

    /** A fresh key each time the client asks for one: k-1, k-2, ... */
    private static Supplier<String> keys() {
        AtomicInteger n = new AtomicInteger();
        return () -> "k-" + n.incrementAndGet();
    }

    /** Delivers every request and every reply. */
    private Refunds.Link direct() {
        return command -> {
            sends.incrementAndGet();
            return service.handle(command);
        };
    }

    /** Delivers every request; the reply to the first one is lost. */
    private Refunds.Link losesFirstReply() {
        return command -> {
            String reply = service.handle(command);
            if (sends.incrementAndGet() == 1) throw new TimeoutException("no reply within 2 s");
            return reply;
        };
    }

    private String refund(Refunds.Link link, String orderId, long cents) {
        Refunds.RefundClient client = new Refunds.RefundClient(link, keys(), 3);
        return assertTimeoutPreemptively(LIMIT, () -> client.refund(orderId, cents));
    }

    @Test
    void aRefundIsPaidAndItsReceiptReturned() {
        String receipt = refund(direct(), "o-4471", 1899);

        assertThat(service.payouts()).hasSize(1);
        assertThat(service.payouts().get(0).orderId()).isEqualTo("o-4471");
        assertThat(receipt).isEqualTo(service.payouts().get(0).receipt());
    }

    @Test
    void refundsOnDifferentOrdersAreAllPaid() {
        Refunds.RefundClient client = new Refunds.RefundClient(direct(), keys(), 3);

        assertTimeoutPreemptively(LIMIT, () -> {
            client.refund("o-3010", 500);
            client.refund("o-3011", 500);
        });

        assertThat(service.payouts()).extracting(Refunds.Payout::orderId).containsExactly("o-3010", "o-3011");
    }

    @Test
    void aLinkThatNeverAnswersEndsInNoReplyAfterEveryAttempt() {
        Refunds.Link silent = command -> {
            sends.incrementAndGet();
            throw new TimeoutException("no reply within 2 s");
        };
        Refunds.RefundClient client = new Refunds.RefundClient(silent, keys(), 3);

        assertThatThrownBy(() -> assertTimeoutPreemptively(LIMIT, () -> client.refund("o-7730", 4200)))
                .isInstanceOf(Refunds.NoReplyException.class);
        assertThat(sends.get()).isEqualTo(3);
    }

    @Test
    void aLostReplyIsRetriedWithoutPayingTwice() {
        refund(losesFirstReply(), "o-5120", 2500);

        assertThat(sends.get()).isEqualTo(2);
        assertThat(service.payouts()).hasSize(1);
    }

    @Test
    void twoRefundsOfTheSameAmountOnOneOrderAreBothPaid() {
        Refunds.RefundClient client = new Refunds.RefundClient(direct(), keys(), 3);

        String first = assertTimeoutPreemptively(LIMIT, () -> client.refund("o-6002", 750));
        String second = assertTimeoutPreemptively(LIMIT, () -> client.refund("o-6002", 750));

        assertThat(service.payouts()).hasSize(2);
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void theRetryGetsTheReceiptOfTheRefundThatWasPaid() {
        String receipt = refund(losesFirstReply(), "o-8815", 1300);

        assertThat(service.payouts()).isNotEmpty();
        assertThat(receipt).isEqualTo(service.payouts().get(0).receipt());
    }
}
