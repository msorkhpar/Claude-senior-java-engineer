The page's Q6 contrasts a GoF Singleton, which hard-codes its dependency and
is reached through `getInstance()`, with a service that **receives its
dependency through the constructor**. The second one can be tested with any
repository, because it holds no hidden global state.

`UserRepository` (given) has `String findById(String id)`, which returns
`null` for an unknown id. Write `UserService`:

- `new UserService(repository)` keeps the repository it is given; `null` is
  refused at once with `NullPointerException`.
- `findUser(id)` returns `Optional.of(name)` from the repository, or
  `Optional.empty()` when the repository returns `null`.
- Two services with different repositories never see each other's data.

| repository knows | call | answer |
|---|---|---|
| `42 -> ada` | `findUser("42")` | `Optional[ada]` |
| `42 -> ada` | `findUser("7")` | `Optional.empty` |
| nothing | `new UserService(null)` | `NullPointerException` |
