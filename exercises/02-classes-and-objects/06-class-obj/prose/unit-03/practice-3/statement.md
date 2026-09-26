A **static factory method** is a static method that returns an instance, used
instead of a public constructor. Unlike a constructor it has a name, and it may
return an object that already exists rather than always making a new one.

Write `Color`:

- `static Color of(int red, int green, int blue)`: each part is between `0` and
  `255`, both included. Anything else throws `IllegalArgumentException`.
- `static Color fromHex(String hex)` reads `#RRGGBB` (upper or lower case), for
  example `#FF8000`.
- `red()`, `green()` and `blue()` return the parts.
- Both factories hand out **one shared instance per distinct color**: asking again
  for the same parts returns the same object (`==`), not a new one.
- No constructor is public: the factories are the only way in.

**Examples**

```
Color.of(255, 128, 0).green()               -> 128
Color.fromHex("#ff8000") == Color.of(255, 128, 0) -> true
Color.of(256, 0, 0)                         -> IllegalArgumentException
```
