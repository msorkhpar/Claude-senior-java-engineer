package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.PaymentScreen.Decision.ACCEPT;
import static practice.PaymentScreen.Decision.DECLINE;
import static practice.PaymentScreen.Decision.HOLD;
import static practice.PaymentScreen.Decision.REVIEW;

class PaymentScreenTest {

    /** Scores by customer, and records every billed call. */
    private final List<String> billed = new ArrayList<>();
    private final Map<String, Integer> scores = Map.of(
            "ana", 12, "bo", 59, "chen", 60, "dara", 89, "eli", 90, "fay", 97);
    private final PaymentScreen screen = new PaymentScreen(card -> {
        billed.add(card.customer());
        return scores.get(card.customer());
    });

    @Test
    void decidesEveryKindOfPayment() {
        assertThat(screen.screen(new PaymentScreen.Transfer("gus", 42_000))).isEqualTo(ACCEPT);
        assertThat(screen.screen(new PaymentScreen.Transfer("gus", 500_000))).isEqualTo(ACCEPT);
        assertThat(screen.screen(new PaymentScreen.Transfer("gus", 500_001))).isEqualTo(REVIEW);
        assertThat(screen.screen(new PaymentScreen.Transfer("hal", 3_200_000))).isEqualTo(REVIEW);
        assertThat(screen.screen(new PaymentScreen.Voucher("SPRING-26", 9_999_999))).isEqualTo(ACCEPT);
        assertThat(screen.screen(new PaymentScreen.Card("ana", 1_500))).isEqualTo(ACCEPT);
        assertThat(screen.screen(new PaymentScreen.Card("bo", 1_500))).isEqualTo(ACCEPT);
        assertThat(screen.screen(new PaymentScreen.Card("chen", 1_500))).isEqualTo(REVIEW);
        assertThat(screen.screen(new PaymentScreen.Card("dara", 1_500))).isEqualTo(REVIEW);
        assertThat(screen.screen(new PaymentScreen.Card("eli", 1_500))).isEqualTo(DECLINE);
        assertThat(screen.screen(new PaymentScreen.Card("fay", 7_000_000))).isEqualTo(DECLINE);
    }

    @Test
    void holdsTransfersAboveTheHoldLimit() {
        assertThat(screen.screen(new PaymentScreen.Transfer("ivo", 5_000_000))).isEqualTo(REVIEW);
        assertThat(screen.screen(new PaymentScreen.Transfer("ivo", 5_000_001))).isEqualTo(HOLD);
        assertThat(screen.screen(new PaymentScreen.Transfer("jun", 80_000_000))).isEqualTo(HOLD);
    }

    @Test
    void scoresEachCardPaymentOnceAndNothingElse() {
        screen.screen(new PaymentScreen.Transfer("gus", 42_000));
        screen.screen(new PaymentScreen.Voucher("SPRING-26", 2_000));
        assertThat(billed).as("score calls for a transfer and a voucher").isEmpty();

        screen.screen(new PaymentScreen.Card("ana", 1_500));
        screen.screen(new PaymentScreen.Card("chen", 1_500));
        screen.screen(new PaymentScreen.Card("fay", 1_500));
        assertThat(billed).as("score calls for three card payments").containsExactly("ana", "chen", "fay");
    }
}
