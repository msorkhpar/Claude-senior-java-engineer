The page's second pitfall opens a `Statement` and a `ResultSet` and closes
them by hand at the end, so an exception in between leaks them. The fix is
try-with-resources. There is a catch: this method is **lent** a
`Connection`. It closes what it opens, and the connection belongs to the
caller.

Write `NameQuery.names(connection, department)`: the names in
`employees(id, name, department)` for that department, ordered by id.

- **The `PreparedStatement` and its `ResultSet` are closed when the method
  returns**, and **they are closed even when reading a row throws** (the
  `SQLException` still reaches the caller).
- **The caller's connection is left open.**

| rows (id, name, department) | call | answer |
|---|---|---|
| 1 Ada Engineering, 2 Grace Sales, 3 Linus Engineering | `names(c, "Engineering")` | `[Ada, Linus]` |
