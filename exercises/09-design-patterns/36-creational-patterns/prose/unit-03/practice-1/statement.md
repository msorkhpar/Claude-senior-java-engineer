A constructor like `new User("Alice", 25, "alice@example.com", true)` makes
the reader count arguments. A **fluent builder** names each step and returns
`this`, and the page's rule is to **validate in `build()`**: the builder may
hold anything while it is being filled, but no invalid `User` is ever created.

Write `User` with a private constructor and a static nested `Builder`
(`User.builder()` returns a new one) with `name`, `age`, `email`, `active`
and `build()`:

- `name` is required; `null`, `""` or only spaces makes `build()` throw
  `IllegalStateException`.
- `age` defaults to `0` and must be `>= 0` when `build()` runs.
- `email` is optional: `email()` returns `Optional.empty()` when unset.
- `active` defaults to `true`.

| chain | result |
|---|---|
| `.name("Alice").age(25).email("alice@example.com").active(false)` | Alice, 25, `Optional[alice@example.com]`, false |
| `.name("Bob")` | Bob, 0, `Optional.empty`, true |
| `.name("  ")` | `IllegalStateException` |
| `.name("Cy").age(-1)` | `IllegalStateException` |
| `.name("Di").age(-1).age(30)` | Di, 30 |
| `.name("  ").name("Eve")` | Eve |
