Text blocks make SQL readable, and `formatted()` makes it easy to fill in. The
page's warning: `formatted()` has no SQL escaping, so a value typed by a user
must never be formatted into a query. Its safe version keeps a `?` placeholder
and binds the value later with a `PreparedStatement`.

Write `Sql.select(String table, List<String> columns, String filterColumn)`.
It returns these three lines joined by `\n`, with no newline at the end:

```text
SELECT <columns>
FROM <table>
WHERE <filterColumn> = ?
```

- `<columns>` is the column names joined by `, `, or `*` when the list is
  empty.
- The value to filter by never appears: the query keeps the `?`.
- Names are code, not data, so each one is checked: `table`, every column and
  `filterColumn` must each be **one whole identifier**, a letter or `_`
  followed by letters, digits or `_` (ASCII only; no `$`, no line break). Anything else throws
  `IllegalArgumentException`.

| table, columns, filterColumn | answer |
|---|---|
| `users`, `[name, email]`, `id` | `SELECT name, email` / `FROM users` / `WHERE id = ?` |
| `orders`, `[]`, `status` | `SELECT *` / `FROM orders` / `WHERE status = ?` |
| `users; DROP TABLE users`, `[name]`, `id` | throws `IllegalArgumentException` |
| `users`, `[name, "1=1 --"]`, `id` | throws `IllegalArgumentException` |

(`/` separates the lines here.)
