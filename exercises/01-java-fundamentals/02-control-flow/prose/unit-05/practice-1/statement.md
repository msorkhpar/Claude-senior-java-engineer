A switch expression's arrow case can list several labels, separated by commas, and it
never falls through into the next case: `case MONDAY, FRIDAY, SUNDAY -> "Relax";`.

Write `points(char grade)` in `GradePoints` as one switch expression. It returns the grade
points of a letter, in either case:

| grade | points |
|---|---|
| `'A'` or `'a'` | `4.0` |
| `'B'` or `'b'` | `3.0` |
| `'C'` or `'c'` | `2.0` |
| `'D'` or `'d'` | `1.0` |
| `'F'` or `'f'` | `0.0` |

Any other character is not a grade: throw `IllegalArgumentException` (a `default` arm may
`throw`).
