package practice;

import java.util.List;
import java.util.function.Function;

public final class PriceRules {

    private PriceRules() {
    }

    /** Each price reduced by {@code percent} per cent, rounded to cents. */
    public static List<Double> discounted(List<Double> prices, double percent) {
        Function<Double, Double> apply = price -> {
            double discount = price * (percent / 100);
            double finalPrice = price - discount;
            return Math.round(finalPrice * 100.0) / 100.0;
        };
        return prices.stream().map(apply).toList();
    }

    /** The band of each price: cheap, moderate, expensive or premium. */
    public static List<String> categorize(List<Double> prices) {
        Function<Double, String> band = price -> {
            if (price < 10) return "cheap";
            else if (price < 100) return "moderate";
            else if (price < 1000) return "expensive";
            else return "premium";
        };
        return prices.stream().map(band).toList();
    }
}

