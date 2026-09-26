Failure atomicity is not automatic: a method that changes several fields and
throws in the middle leaves the object half-updated. Write `Rectangle`:

- `Rectangle(int width, int height)`, `getWidth()`, `getHeight()`;
- `void resize(int newWidth, int newHeight)`.

Both the constructor and `resize` accept positive sides only. A side that is
not positive throws `IllegalArgumentException("Width must be positive: <w>")`
or `IllegalArgumentException("Height must be positive: <h>")`, the width
checked first.

| calls | result |
|---|---|
| `new Rectangle(2, 3)` then `resize(4, 5)` | width `4`, height `5` |
| `new Rectangle(2, 3)` then `resize(10, 0)` | throws `IllegalArgumentException("Height must be positive: 0")`; still `2` by `3` |
| `new Rectangle(0, 3)` | throws `IllegalArgumentException("Width must be positive: 0")` |

If a constructor throws, the caller never gets a reference, so validating
there means an invalid rectangle can never exist.
