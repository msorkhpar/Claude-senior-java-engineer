package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaitListTest {

    @Test
    void cancellingTheFirstTicketMovesTheOthersUp() {
        WaitList shop = new WaitList();
        shop.join(0);
        shop.join(14);
        shop.join(9);

        shop.cancel(0);

        assertThat(shop.waiting()).containsExactly(14, 9);
    }

    @Test
    void releasingAnAccountWithoutAHoldChangesNothing() {
        WaitList shop = new WaitList();
        shop.hold(5_120L, "unpaid invoice");

        shop.release(77);

        assertThat(shop.holds()).containsOnlyKeys(5_120L);
    }

    @Test
    void cancelsTheTicketWithThatNumberWhereverItWaits() {
        WaitList shop = new WaitList();
        shop.join(6);
        shop.join(2);
        shop.join(1);
        shop.join(3);

        shop.cancel(1);
        shop.cancel(3);

        assertThat(shop.waiting()).containsExactly(6, 2);
    }

    @Test
    void releasesTheHoldOnThatAccount() {
        WaitList shop = new WaitList();
        shop.hold(5_120L, "unpaid invoice");
        shop.hold(803L, "card expired");

        shop.release(803);

        assertThat(shop.holds()).containsOnlyKeys(5_120L);
    }
}
