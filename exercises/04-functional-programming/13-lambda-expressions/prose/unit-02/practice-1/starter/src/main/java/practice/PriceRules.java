package practice;

import java.util.List;
import java.util.function.Function;

public final class PriceRules {

    private PriceRules() {
    }

    /** Each price reduced by {@code percent} per cent, rounded to cents. */
    public static List<Double> discounted(List<Double> prices, double percent) {
        throw new UnsupportedOperationException("write discounted");
    }

    /** The band of each price: cheap, moderate, expensive or premium. */
    public static List<String> categorize(List<Double> prices) {
        throw new UnsupportedOperationException("write categorize");
    }
}
