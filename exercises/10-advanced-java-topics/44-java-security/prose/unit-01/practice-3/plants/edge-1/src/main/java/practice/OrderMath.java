package practice;

public final class OrderMath {

    private OrderMath() {
    }

    public static int lineTotal(int unitPriceCents, int quantity) {
        return (int) ((long) unitPriceCents * quantity);
    }

    public static int total(int... lines) {
        int sum = 0;
        for (int line : lines) {
            sum = Math.addExact(sum, line);
        }
        return sum;
    }

    public static int distance(int a, int b) {
        return Math.absExact(Math.subtractExact(a, b));
    }
}
