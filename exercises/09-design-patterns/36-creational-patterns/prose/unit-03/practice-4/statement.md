A fluent builder lets **the client choose the steps and their order**, and the
builder still assembles a well-formed product. `StringBuilder` is the JDK's
builder for text; here you write one for SQL, the kind of string the page
suggests builders for.

Write `SelectBuilder` (`new SelectBuilder()`), each step returning `this`:

- `from(table)`, `columns(String... names)`, `where(condition)` (repeatable;
  conditions are joined with ` AND ` **in the order given**), `orderBy(column)`.
- `build()` returns `SELECT <columns> FROM <table>[ WHERE ...][ ORDER BY ...]`,
  whatever order the steps were called in. No `columns` means `*`. Calling
  `build()` again returns the same query.
- A missing or blank table makes `build()` throw `IllegalStateException`.

| steps | `build()` |
|---|---|
| `from("users")` | `SELECT * FROM users` |
| `from("users").columns("id", "name").where("age > ?").where("active = ?").orderBy("name")` | `SELECT id, name FROM users WHERE age > ? AND active = ? ORDER BY name` |
| `orderBy("id").where("age > ?").from("users")` | `SELECT * FROM users WHERE age > ? ORDER BY id` |
| `columns("id")` | `IllegalStateException` |
