The page's third example mixes two escaping systems. Java needs no escape for a
single quote, but SQL does: it writes a quote inside a string by **doubling**
it, as in `'O''Brien'`. And a `\` that Java source must write as `\\` is just one
character in the finished string.

Write `SqlQuery.of(String table, String column, String value)`. It returns
these four lines joined by `\n`, with no newline after the last, built from
traditional literals (as the page's `buildSqlTraditional` does):

```text
SELECT *
FROM <table>
WHERE <column> = '<value>'
ORDER BY id;
```

Inside the quoted value, each `'` is written `''`; every other character goes
in as it is.

| table, column, value | the WHERE line |
|---|---|
| `users`, `name`, `Alice` | `WHERE name = 'Alice'` |
| `users`, `name`, `O'Brien` | `WHERE name = 'O''Brien'` |
| `files`, `path`, `C:\temp` | `WHERE path = 'C:\temp'` |
