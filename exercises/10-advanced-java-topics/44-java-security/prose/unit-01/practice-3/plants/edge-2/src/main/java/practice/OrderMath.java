package practice;

public final class OrderMath {

    private OrderMath() {
    }

    public static int lineTotal(int unitPriceCents, int quantity) {
        return Math.multiplyExact(unitPriceCents, quantity);
    }

    public static int total(int... lines) {
        long sum = 0;
        for (int line : lines) {
            sum += line;
        }
        return Math.toIntExact(sum);
    }

    public static int distance(int a, int b) {
        return Math.absExact(Math.subtractExact(a, b));
    }
}
