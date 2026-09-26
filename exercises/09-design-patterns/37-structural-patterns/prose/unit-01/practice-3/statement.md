The page's data-format adapter sits between an old system that stores a user
as a `String[]` (`[first, last, email]`) and a new one that expects a
`ModernUser(fullName, email)` record. Its edge-case list asks the adapter to
handle **extra data fields**, **too few fields** and **empty collections**.

Given: the records `LegacyUser(String[] data)` and `ModernUser(String fullName,
String email)`. Write `UserDataAdapter`:

- `adapt(user)` returns `ModernUser(first + " " + last, email)`, reading the
  email at index 2; **fields beyond the third are ignored**.
- **Fewer than three fields is refused with `IllegalArgumentException`** (not an
  `ArrayIndexOutOfBoundsException`).
- `adaptAll(users)` adapts each user in order; **an empty list adapts to an
  empty list**.

| legacy data | `adapt` gives |
|---|---|
| `["Ada", "Lovelace", "ada@example.org"]` | `ModernUser[fullName=Ada Lovelace, email=ada@example.org]` |
| `["Alan", "Turing", "alan@example.org", "x"]` | `ModernUser[fullName=Alan Turing, email=alan@example.org]` |
| `["Grace", "Hopper"]` | `IllegalArgumentException` |

| call | answer |
|---|---|
| `adaptAll(List.of())` | `[]` |
