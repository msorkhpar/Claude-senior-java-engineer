package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeCostsTest {

    private static final Map<String, Integer> RAW = Map.of(
            "flour", 40, "butter", 95, "egg", 30, "milk", 25, "salt", 2, "basil", 60, "pine nuts", 210,
            "pasta", 70);

    private static final Map<String, List<String>> RECIPES = Map.of(
            "pesto", List.of("basil", "pine nuts", "salt"),
            "pancakes", List.of("flour", "egg", "milk", "butter"),
            "pasta al pesto", List.of("pasta", "pesto", "salt"),
            "pesto scones", List.of("flour", "butter", "pesto"));

    @Test
    void aRawIngredientCostsItsPriceEveryTime() {
        RecipeCosts costs = new RecipeCosts(RECIPES, RAW);

        assertThat(costs.cost("butter")).isEqualTo(95);
        assertThat(costs.cost("pine nuts")).isEqualTo(210);
        assertThat(costs.cost("butter")).isEqualTo(95);
    }

    @Test
    void aRecipeWhoseIngredientsWereNeverCostedIsCosted() {
        RecipeCosts costs = new RecipeCosts(RECIPES, RAW);

        assertThat(costs.cost("pancakes")).isEqualTo(40 + 30 + 25 + 95);
        assertThat(costs.cost("pasta al pesto")).isEqualTo(70 + (60 + 210 + 2) + 2);
    }

    @Test
    void eachItemIsWorkedOutOnlyOnce() {
        RecipeCosts costs = new RecipeCosts(RECIPES, RAW);
        for (String item : List.of("basil", "pine nuts", "salt", "pasta", "flour", "butter")) costs.cost(item);
        costs.cost("pesto");
        int before = costs.workedOut;

        costs.cost("pasta al pesto");
        costs.cost("pesto scones");
        costs.cost("pasta al pesto");

        assertThat(costs.workedOut - before).isEqualTo(2);       // the two dishes; pesto was known
    }
}
