package practice;

public class Cafe {

    /** Give each size its own base price: SMALL 300, MEDIUM 350, LARGE 425. */
    public enum Size {
        SMALL, MEDIUM, LARGE;

        public long baseCents() {
            throw new UnsupportedOperationException("write baseCents");
        }
    }

    public sealed interface Order permits Drink, Pastry, GiftCard {
    }

    public record Drink(Size size, int shots) implements Order {
    }

    public record Pastry(String name, int count) implements Order {
    }

    public record GiftCard(long cents) implements Order {
    }

    public static long priceCents(Order order) {
        throw new UnsupportedOperationException("write priceCents");
    }
}
