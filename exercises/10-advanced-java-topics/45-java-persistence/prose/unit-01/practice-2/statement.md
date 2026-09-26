`rs.getInt("age")` returns `0` for SQL NULL, the same as a real age of 0.
The page's Q4 tells them apart with `wasNull()` right after the getter, or
with `getObject("age", Integer.class)`. Reference types such as
`getString` already return `null`.

Write `PersonRows.map(ResultSet rows)`: it reads the **current** row (the
caller has already called `next()`) and returns a `Person(id, name, age, email)`.

- **A SQL NULL age maps to `null`, not 0**, and **a real age of 0 stays 0**.
- A NULL email maps to `null`.
- **Columns are read by name**, so the SELECT's column order does not matter.

| row (id, name, age, email) | `map(rows)` |
|---|---|
| 1, Ada, 36, ada@example.org | `Person[id=1, name=Ada, age=36, email=ada@example.org]` |
| 2, Linus, NULL, NULL | `Person[id=2, name=Linus, age=null, email=null]` |
| 3, Baby, 0, NULL | `Person[id=3, name=Baby, age=0, email=null]` |
