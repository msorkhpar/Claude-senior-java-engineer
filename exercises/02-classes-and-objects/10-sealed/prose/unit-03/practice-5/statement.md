The page recommends sealed interfaces for API contracts: the library fixes the
set of implementations, so every caller can handle all of them with an
exhaustive `switch`, and no client can add a surprise variant. `Ages` offers
such a contract:

```text
sealed interface Result permits Ok, Err
record Ok(int value)       record Err(String message)
```

Write:

1. `static Result parseAge(String text)`:
   - `Err("not a number: " + text)` when `text` is not an integer;
   - `Err("out of range: " + value)` when the integer is below 0 or above 150;
   - otherwise `Ok(value)`.
2. `static Result sumAges(List<String> texts)`: parse every text in order. If
   all are `Ok`, return `Ok` of their sum (`Ok(0)` for no texts); otherwise
   return the `Err` of the **first** text that failed.

Handle each `Result` with a `switch`; `Result` is sealed, so no `default` is needed.

**Examples**

- `parseAge("42")` -> `Ok[value=42]`
- `parseAge("abc")` -> `Err[message=not a number: abc]`
- `parseAge("200")` -> `Err[message=out of range: 200]`
- `sumAges(["30", "12"])` -> `Ok[value=42]`
