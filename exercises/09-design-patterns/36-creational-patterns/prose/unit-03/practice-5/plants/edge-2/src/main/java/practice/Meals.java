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

    private abstract static class CourseBuilder implements MealBuilder {
        private String drink = "none";
        private String mainCourse = "none";
        private String dessert = "none";

        void drink(String value) {
            drink = value;
        }

        void mainCourse(String value) {
            mainCourse = value;
        }

        void dessert(String value) {
            dessert = value;
        }

        public Meal getMeal() {
            Meal meal = new Meal(drink, mainCourse, dessert);
            drink = "none";
            mainCourse = "none";
            dessert = "none";
            return meal;
        }
    }

    public static final class HealthyMealBuilder extends CourseBuilder {
        public void buildDrink() {
            drink("Water");
        }

        public void buildMainCourse() {
            mainCourse("Grilled chicken");
        }

        public void buildDessert() {
            dessert("Fruit salad");
        }
    }

    public static final class ClassicMealBuilder extends CourseBuilder {
        public void buildDrink() {
            drink("Soda");
        }

        public void buildMainCourse() {
            mainCourse("Burger");
        }

        public void buildDessert() {
            dessert("Ice cream");
        }
    }

    public static final class Director {
        public Meal kidsMeal(MealBuilder builder) {
            builder.buildDessert();
            builder.buildMainCourse();
            builder.buildDrink();
            return builder.getMeal();
        }

        public Meal lightMeal(MealBuilder builder) {
            builder.buildDrink();
            builder.buildMainCourse();
            return builder.getMeal();
        }
    }
}
