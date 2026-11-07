A bakery's costing tool works out what each item on the menu costs to make. A recipe's cost is
the sum of its ingredients' costs, and an ingredient can be a recipe of its own: the pesto in
pasta al pesto. Working an item out is slow in the real tool, so each cost is worked out once and
remembered in the `known` map.

Write `cost(item)` in `src/main/java/practice/RecipeCosts.java`. `workOut(item)` already computes
a cost from scratch, calling `cost(...)` for each ingredient.

When you're done, any item can be costed, whatever has or hasn't been costed before it, and no
item is worked out twice. About five lines.
