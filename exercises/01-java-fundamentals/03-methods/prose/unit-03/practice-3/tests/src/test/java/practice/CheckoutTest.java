package practice;

import org.junit.jupiter.api.Test;
import practice.Checkout.Basket;
import practice.Checkout.Item;
import practice.Checkout.PlacedOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckoutTest {

    private static final Item OAT_MILK = new Item("OAT-1L", 2);
    private static final Item RYE_BREAD = new Item("RYE-800", 1);
    private static final Item APPLES = new Item("APL-KG", 3);
    private static final Item HONEY = new Item("HNY-350", 1);

    @Test
    void anOrderHoldsTheItemsItWasPlacedWith() {
        PlacedOrder order = new PlacedOrder("WEB-5520", new ArrayList<>(List.of(OAT_MILK, APPLES)));

        assertThat(order.number()).isEqualTo("WEB-5520");
        assertThat(order.items()).containsExactly(OAT_MILK, APPLES);
        assertThat(order.units()).isEqualTo(5);
    }

    @Test
    void anEmptyOrderIsRefused() {
        assertThatThrownBy(() -> new PlacedOrder("T2-0300", new ArrayList<>()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theNextCustomerAtTheTillDoesNotChangeTheLastOrder() {
        Basket till = new Basket();
        till.add(HONEY);
        till.add(RYE_BREAD);
        PlacedOrder first = till.place("T4-0017");
        till.add(APPLES);
        PlacedOrder second = till.place("T4-0018");

        assertThat(first.items()).containsExactly(HONEY, RYE_BREAD);
        assertThat(first.units()).isEqualTo(2);
        assertThat(second.items()).containsExactly(APPLES);
    }

    @Test
    void noReaderCanChangeAPlacedOrder() {
        PlacedOrder order = new PlacedOrder("WEB-5522", new ArrayList<>(List.of(RYE_BREAD)));

        try {
            order.items().add(HONEY);
        } catch (UnsupportedOperationException expected) {
            // refusing the change is what a placed order should do
        }

        assertThat(order.items()).containsExactly(RYE_BREAD);
    }

    @Test
    void anOrderWithAMissingItemIsRefused() {
        List<Item> withAGap = new ArrayList<>(Arrays.asList(APPLES, null));

        assertThatThrownBy(() -> new PlacedOrder("WEB-5523", withAGap))
                .isInstanceOf(NullPointerException.class);
    }
}
