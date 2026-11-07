package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** What a dish costs to make, in cents. A recipe may use other recipes, such as a sauce. */
public class RecipeCosts {

    private final Map<String, List<String>> recipes;
    private final Map<String, Integer> rawPrices;
    private final Map<String, Integer> known = new HashMap<>();
    int workedOut;

    /**
     * @param recipes   each recipe's ingredients, which may be raw ingredients or other recipes
     * @param rawPrices the price of each raw ingredient, in cents
     */
    public RecipeCosts(Map<String, List<String>> recipes, Map<String, Integer> rawPrices) {
        this.recipes = recipes;
        this.rawPrices = rawPrices;
    }

    /** Works out one item's cost from scratch; its ingredients are costed through cost(...). */
    int workOut(String item) {
        workedOut++;
        List<String> ingredients = recipes.get(item);
        if (ingredients == null) return rawPrices.get(item);
        int total = 0;
        for (String ingredient : ingredients) total += cost(ingredient);
        return total;
    }

    /** The item's cost, worked out once and remembered in {@code known}. */
    public int cost(String item) {
        throw new IllegalStateException("TODO: the remembered cost, or a newly worked-out one");
    }
}
