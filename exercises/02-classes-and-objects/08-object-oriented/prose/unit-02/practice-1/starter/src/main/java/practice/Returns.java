package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * The returns desk of an online shop. Each returned item goes to the desk, which has a method for
 * each kind of item it knows: books are restocked, gadgets are tested, batteries go to the
 * hazardous-goods shelf. Kinds the desk doesn't know yet go to manual review.
 */
public class Returns {

    /** What the desk does with each kind of returned item. */
    public interface Desk {
        default String handle(Item item) { return "manual review: " + item.sku(); }
        String handle(Book book);
        String handle(Gadget gadget);
        String handle(Battery battery);
    }

    public abstract static class Item {
        private final String sku;

        protected Item(String sku) { this.sku = sku; }

        public String sku() { return sku; }

        /** Hands this item to the desk and returns what the desk did with it. */
        public abstract String accept(Desk desk);
    }

    public static class Book extends Item {
        public Book(String sku) { super(sku); }

        @Override
        public String accept(Desk desk) {
            // TODO: hand this book to the desk method written for books.
            throw new IllegalStateException("TODO: Book.accept");
        }
    }

    public static class Gadget extends Item {
        public Gadget(String sku) { super(sku); }

        @Override
        public String accept(Desk desk) {
            // TODO: hand this gadget to the desk method written for gadgets.
            throw new IllegalStateException("TODO: Gadget.accept");
        }
    }

    /** A battery is a gadget, with shipping rules of its own. */
    public static class Battery extends Gadget {
        public Battery(String sku) { super(sku); }
    }

    /** Hands every item of a returned cart to the desk, in order, and returns what it did with each. */
    public static List<String> route(List<? extends Item> cart, Desk desk) {
        // TODO: every item of the cart, in order, must reach the desk method for its own kind.
        throw new IllegalStateException("TODO: route");
    }
}
