In a switch statement, `break` ends the case. Forget it, and execution runs on into the
next case, which then overwrites what the first one set.

Write `fee(int zone)` in `Shipping`, with a `switch` statement:

| zone | fee |
|---|---|
| 1 | `5` |
| 2 | `8` |
| 3 | `12` |

Any other zone throws `IllegalArgumentException`.
