package practice;

import java.util.ArrayList;
import java.util.List;

/** The shop's checkout: a basket that is filled, placed as an order, and reused. */
public class Checkout {

    public record Item(String sku, int quantity) { }

    /** An order as it was placed. Billing, packing and the customer's receipt all read it. */
    public record PlacedOrder(String number, List<Item> items) {
        public PlacedOrder {
            // TODO: An order is never empty, and once placed it stays exactly as it was placed,
            //       for every reader, whatever happens to the list it was placed from.
            throw new IllegalStateException("TODO: PlacedOrder");
        }

        public int units() {
            int units = 0;
            for (Item item : items) units += item.quantity();
            return units;
        }
    }

    /** One basket per till; it is emptied after each order and filled again for the next customer. */
    public static class Basket {
        private final List<Item> items = new ArrayList<>();

        public void add(Item item) {
            items.add(item);
        }

        public PlacedOrder place(String number) {
            PlacedOrder order = new PlacedOrder(number, items);
            items.clear();
            return order;
        }
    }
}
