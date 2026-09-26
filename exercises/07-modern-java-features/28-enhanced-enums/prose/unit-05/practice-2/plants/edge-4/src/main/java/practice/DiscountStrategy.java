package practice;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public enum DiscountStrategy {
    NONE("No Discount", amount -> amount),
    PERCENTAGE_10("10% Off", amount -> amount * 0.90),
    PERCENTAGE_20("20% Off", amount -> amount * 0.80),
    FLAT_5("5 Off", amount -> amount - 5.0),
    FLAT_10("10 Off", amount -> amount - 10.0),
    BUY_ONE_GET_HALF("Buy 1 Get 50% Off 2nd", amount -> amount * 0.75);

    private final String description;
    private final Function<Double, Double> calculator;

    DiscountStrategy(String description, Function<Double, Double> calculator) {
        this.description = description;
        this.calculator = calculator;
    }

    public String description() {
        return description;
    }

    /** The discounted price: amount validated, result rounded to cents and never below zero. */
    public double applyDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return Math.max(0, Math.round(calculator.apply(amount) * 100.0) / 100.0);
    }

    /** The strategy other than NONE that leaves the lowest price; the first declared wins a tie. */
    public static DiscountStrategy bestDiscount(double amount) {
        DiscountStrategy best = NONE;
        double lowest = Double.MAX_VALUE;
        for (DiscountStrategy d : values()) {
            if (d != NONE && d.applyDiscount(amount) <= lowest) {
                best = d;
                lowest = d.applyDiscount(amount);
            }
        }
        return best;
    }
}
