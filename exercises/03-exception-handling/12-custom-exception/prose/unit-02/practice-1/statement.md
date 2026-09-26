Since Java 14 a `switch` expression may use `throw` as the body of a case,
which makes rejecting an unexpected value a one-liner; a `switch` expression
is a natural fit here. Write `LevelParser.parseLevel(String s)`:

| call | result |
|---|---|
| `parseLevel("low")` | `1` |
| `parseLevel("high")` | `3` |
| `parseLevel("mid")` | throws `IllegalArgumentException("Unknown level: mid")` |

A message should say what was wrong *and with which value*. Every input that is
not a known level, whatever it is, gets the same exception; remember what a
plain `switch` does with `null`, and that Java 21 lets a case say otherwise.
