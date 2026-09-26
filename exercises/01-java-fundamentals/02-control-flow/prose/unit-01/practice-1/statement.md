An `else if` chain tests its conditions from the top and runs only the first branch that
holds, so the order of the conditions matters, and so does whether each boundary uses
`>=` or `>`.

Write `letter(int score)` in `Grades`. It returns the letter for a score from `0` to `100`:

| score | letter |
|---|---|
| 90 to 100 | `"A"` |
| 80 to 89 | `"B"` |
| 70 to 79 | `"C"` |
| 60 to 69 | `"D"` |
| 0 to 59 | `"F"` |

A score below `0` or above `100` is not a score: return `"Invalid"` for it.

Examples: `letter(95)` is `"A"`, `letter(90)` is `"A"`, `letter(89)` is `"B"`,
`letter(59)` is `"F"`, `letter(101)` is `"Invalid"`.
