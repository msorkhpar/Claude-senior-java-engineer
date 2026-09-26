Parameters carry **values**. The page's first best practice warns that they
cannot carry identifiers or keywords: a user-chosen `ORDER BY` column or sort
direction cannot be a `?`. Those parts must be **chosen from an allow-list of
known names**, and anything else refused, never pasted into the SQL.

Write `SortedQuery.sql(column, direction)` that returns

```
SELECT id, name, email FROM users ORDER BY <column> <DIRECTION>
```

- `column` must be **exactly** one of `name`, `email`, `created_at`;
- `direction` is `asc` or `desc` in any case, written in upper case;
- anything else, `null` included, throws `IllegalArgumentException`.

| column | direction | result |
|---|---|---|
| `name` | `asc` | `... ORDER BY name ASC` |
| `created_at` | `DESC` | `... ORDER BY created_at DESC` |
| `name; DROP TABLE users` | `asc` | `IllegalArgumentException` |
| `email` | `desc, (SELECT 1)` | `IllegalArgumentException` |
