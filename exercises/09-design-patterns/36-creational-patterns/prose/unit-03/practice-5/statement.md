In the GoF Builder, a **Director** knows the order of the steps and the
**builders** know what each step produces, so one director makes different
meals from different builders. The page's pitfall is a builder reused for a
second product that still carries the first one's state.

`Meal` (given) is a record `(drink, mainCourse, dessert)`; a missing course is
`"none"`. `MealBuilder` (given) has `buildDrink()`, `buildMainCourse()`,
`buildDessert()` and `getMeal()`. Write:

- `HealthyMealBuilder` (Water, Grilled chicken, Fruit salad) and
  `ClassicMealBuilder` (Soda, Burger, Ice cream). `getMeal()` returns the meal
  built so far and **starts the builder afresh**.
- `Director.kidsMeal(builder)`: drink, main course, dessert, then `getMeal()`.
- `Director.lightMeal(builder)`: drink and main course only, then `getMeal()`.

| call | meal |
|---|---|
| `kidsMeal(new HealthyMealBuilder())` | `Meal[Water, Grilled chicken, Fruit salad]` |
| `kidsMeal(new ClassicMealBuilder())` | `Meal[Soda, Burger, Ice cream]` |
| `lightMeal(b)` right after `kidsMeal(b)` | `Meal[Water, Grilled chicken, none]` |
