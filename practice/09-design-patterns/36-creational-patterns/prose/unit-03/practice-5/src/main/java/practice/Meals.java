package practice;

public final class Meals {

    private Meals() {
    }

    public record Meal(String drink, String mainCourse, String dessert) {
    }

    public interface MealBuilder {
        void buildDrink();

        void buildMainCourse();

        void buildDessert();

        /** Returns the meal built so far and starts afresh. */
        Meal getMeal();
    }

    public static final class HealthyMealBuilder implements MealBuilder {
        public void buildDrink() {
            throw new UnsupportedOperationException("write buildDrink");
        }

        public void buildMainCourse() {
            throw new UnsupportedOperationException("write buildMainCourse");
        }

        public void buildDessert() {
            throw new UnsupportedOperationException("write buildDessert");
        }

        public Meal getMeal() {
            throw new UnsupportedOperationException("write getMeal");
        }
    }

    public static final class ClassicMealBuilder implements MealBuilder {
        public void buildDrink() {
            throw new UnsupportedOperationException("write buildDrink");
        }

        public void buildMainCourse() {
            throw new UnsupportedOperationException("write buildMainCourse");
        }

        public void buildDessert() {
            throw new UnsupportedOperationException("write buildDessert");
        }

        public Meal getMeal() {
            throw new UnsupportedOperationException("write getMeal");
        }
    }

    public static final class Director {
        public Meal kidsMeal(MealBuilder builder) {
            throw new UnsupportedOperationException("write kidsMeal");
        }

        public Meal lightMeal(MealBuilder builder) {
            throw new UnsupportedOperationException("write lightMeal");
        }
    }
}
