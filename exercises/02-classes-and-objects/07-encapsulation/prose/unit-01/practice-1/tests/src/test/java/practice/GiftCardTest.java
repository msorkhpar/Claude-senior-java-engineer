package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiftCardTest {

    private static GiftCard card(String number, long cents) {
        GiftCard card = new GiftCard(number);
        if (cents > 0) card.load(cents);
        return card;
    }

    @Test
    void movesTheAmountBetweenTwoOpenCards() {
        GiftCard birthday = card("GC-7301", 12_000);
        GiftCard groceries = card("GC-7302", 3_000);

        birthday.transferTo(groceries, 4_250);

        assertThat(birthday.balance()).isEqualTo(7_750);
        assertThat(groceries.balance()).isEqualTo(7_250);
    }

    @Test
    void refusesMoreThanTheSenderHolds() {
        GiftCard small = card("GC-4410", 1_000);
        GiftCard other = card("GC-4411", 200);

        assertThatThrownBy(() -> small.transferTo(other, 1_001))
                .isInstanceOf(GiftCard.RefusedException.class);

        assertThat(small.balance()).isEqualTo(1_000);
        assertThat(other.balance()).isEqualTo(200);
    }

    @Test
    void aCancelledCardReceivesNothing() {
        GiftCard sender = card("GC-5120", 9_000);
        GiftCard lost = card("GC-5121", 2_000);
        lost.cancel();

        assertThatThrownBy(() -> sender.transferTo(lost, 600))
                .isInstanceOf(GiftCard.RefusedException.class);

        assertThat(lost.balance()).isEqualTo(2_000);
    }

    @Test
    void aCardIsNeverPushedOverTheCap() {
        GiftCard sender = card("GC-6200", 5_000);
        GiftCard nearlyFull = card("GC-6201", GiftCard.CAP - 1_000);

        assertThatThrownBy(() -> sender.transferTo(nearlyFull, 1_500))
                .isInstanceOf(GiftCard.RefusedException.class);

        assertThat(nearlyFull.balance()).isEqualTo(GiftCard.CAP - 1_000);
    }

    @Test
    void aRefusedTransferCostsTheSenderNothing() {
        GiftCard sender = card("GC-8830", 7_500);
        GiftCard closed = card("GC-8831", 0);
        closed.cancel();

        assertThatThrownBy(() -> sender.transferTo(closed, 2_000))
                .isInstanceOf(GiftCard.RefusedException.class);

        assertThat(sender.balance()).isEqualTo(7_500);
    }
}
