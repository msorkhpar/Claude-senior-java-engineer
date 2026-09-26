The enhanced `for` loop visits every element in order: `for (int num : numbers) { ... }`.
The course's `filterEvenNumbers` uses it twice, once to count and once to copy.

Write `evens(int[] numbers)` in `Evens`. It returns a new array of the even numbers, in
their original order, duplicates kept:

- `evens({1, 2, 3, 4, 5, 6})` is `{2, 4, 6}`, and `evens({6, 2, 2, 4})` is `{6, 2, 2, 4}`;
- negative numbers count too: `evens({-4, -3, 0, 7})` is `{-4, 0}`;
- `evens({1, 3, 5})` and `evens({})` are `{}`, an empty array, never `null`.
