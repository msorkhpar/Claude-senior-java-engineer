package practice;

public class Cafe {

    public enum Size {
        SMALL(300), MEDIUM(350), LARGE(425);

        private final long baseCents;

        Size(long baseCents) {
            this.baseCents = baseCents;
        }

        public long baseCents() {
            return baseCents;
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
        return switch (order) {
            case Drink d -> d.size().baseCents() + 50L * (d.shots() - 1);
            case Pastry p -> 250L * p.count();
            case GiftCard g -> g.cents();
        };
    }
}
