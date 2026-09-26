package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import practice.Meals.ClassicMealBuilder;
import practice.Meals.Director;
import practice.Meals.HealthyMealBuilder;
import practice.Meals.Meal;
import practice.Meals.MealBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class MealsTest {

    @Test
    void theDirectorBuildsAKidsMealWithEitherBuilder() {
        Director director = new Director();

        assertThat(director.kidsMeal(new HealthyMealBuilder())).isEqualTo(new Meal("Water", "Grilled chicken", "Fruit salad"));
        assertThat(director.kidsMeal(new ClassicMealBuilder())).isEqualTo(new Meal("Soda", "Burger", "Ice cream"));
    }

    @Test
    void aLightMealAfterAKidsMealHasNoDessert() {
        Director director = new Director();
        MealBuilder builder = new HealthyMealBuilder();
        director.kidsMeal(builder);

        assertThat(director.lightMeal(builder)).isEqualTo(new Meal("Water", "Grilled chicken", "none"));

        builder.buildMainCourse();
        assertThat(builder.getMeal()).isEqualTo(new Meal("none", "Grilled chicken", "none"));

        MealBuilder busy = new HealthyMealBuilder();
        busy.buildDrink();
        assertThat(new HealthyMealBuilder().getMeal()).isEqualTo(new Meal("none", "none", "none"));
    }

    @Test
    void theDirectorFixesTheStepOrder() {
        List<String> calls = new ArrayList<>();
        MealBuilder recording = new MealBuilder() {
            public void buildDrink() {
                calls.add("drink");
            }

            public void buildMainCourse() {
                calls.add("main");
            }

            public void buildDessert() {
                calls.add("dessert");
            }

            public Meal getMeal() {
                calls.add("get");
                return new Meal("d", "m", "s");
            }
        };

        new Director().kidsMeal(recording);

        assertThat(calls).containsExactly("drink", "main", "dessert", "get");
    }
}
