package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnsTest {

    /** A desk that says which of its methods ran, and remembers every item it saw. */
    static class RecordingDesk implements Returns.Desk {
        final List<String> seen = new ArrayList<>();

        @Override
        public String handle(Returns.Item item) {
            seen.add(item.sku());
            return Returns.Desk.super.handle(item);
        }

        @Override
        public String handle(Returns.Book book) {
            seen.add(book.sku());
            return "restock " + book.sku();
        }

        @Override
        public String handle(Returns.Gadget gadget) {
            seen.add(gadget.sku());
            return "bench test " + gadget.sku();
        }

        @Override
        public String handle(Returns.Battery battery) {
            seen.add(battery.sku());
            return "hazmat shelf " + battery.sku();
        }
    }

    @Test
    void everyItemOfACartReachesTheDeskOnceInOrder() {
        RecordingDesk desk = new RecordingDesk();
        List<Returns.Item> cart = List.of(new Returns.Gadget("GD-310"), new Returns.Book("BK-77"),
                new Returns.Battery("BT-9"), new Returns.Book("BK-12"));

        List<String> outcomes = Returns.route(cart, desk);

        assertThat(desk.seen).containsExactly("GD-310", "BK-77", "BT-9", "BK-12");
        assertThat(outcomes).hasSize(4);
        for (int i = 0; i < 4; i++) {
            assertThat(outcomes.get(i)).endsWith(cart.get(i).sku());
        }
    }

    @Test
    void aBookAndAGadgetEachReachTheirOwnMethod() {
        RecordingDesk desk = new RecordingDesk();

        assertThat(new Returns.Book("BK-404").accept(desk)).isEqualTo("restock BK-404");
        assertThat(new Returns.Gadget("GD-55").accept(desk)).isEqualTo("bench test GD-55");
    }

    @Test
    void aBatteryReachesTheBatteryMethodNotTheGadgetOne() {
        RecordingDesk desk = new RecordingDesk();

        assertThat(new Returns.Battery("BT-21").accept(desk)).isEqualTo("hazmat shelf BT-21");
    }

    @Test
    void routingACartSendsEachItemToTheMethodForItsKind() {
        RecordingDesk desk = new RecordingDesk();
        List<Returns.Item> cart = List.of(new Returns.Book("BK-3"), new Returns.Battery("BT-60"),
                new Returns.Gadget("GD-8"));

        assertThat(Returns.route(cart, desk)).containsExactly("restock BK-3", "hazmat shelf BT-60", "bench test GD-8");
    }
}
