`+` turns every other operand into text once a String is involved, and it works left to
right: `"Sum: " + 2 + 3` is `"Sum: 23"`, while `"Sum: " + (2 + 3)` is `"Sum: 5"`. Two `char`s
added together are numbers, not text: `'A' + 'B'` is `131`.

Write three methods in `Mixed`:

1. `describe(int number, boolean flag, double value)`: `describe(42, true, 3.14)` is
   `"Number: 42, Flag: true, Value: 3.14"`.
2. `total(String label, int a, int b)`: `total("Sum", 2, 3)` is `"Sum: 5"`.
3. `initials(char first, char last)`: `initials('A', 'B')` is `"AB"`.
