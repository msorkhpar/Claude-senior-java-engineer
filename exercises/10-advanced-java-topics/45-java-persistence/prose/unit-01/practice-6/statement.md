The page's Q7 opens a `TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY` result set
and moves with `last()`, `getRow()` and `absolute(row)`. Rows are numbered
from 1; `absolute(-1)` means "the last row", counting from the end.

Write `Roster`, over `employees(id, name, department)`, with rows ordered by id:

- `count(connection, department)`: the number of rows. **Jump to the last row
  and read `getRow()`**; do not step through the rows with `next()`. No rows
  gives 0.
- `nameAt(connection, department, position)`: the name at the 1-based
  position. **A position past the last row gives `Optional.empty()`**, and **a
  position below 1 is refused** with `IllegalArgumentException`, because
  `absolute()` would read a negative row from the end.

Close the statement and the result set; the connection is the caller's.

| Engineering rows, by id | call | answer |
|---|---|---|
| Ada, Linus, Barbara | `count(c, "Engineering")` | `3` |
| same | `count(c, "Legal")` | `0` |
| same | `nameAt(c, "Engineering", 2)` | `Optional[Linus]` |
| same | `nameAt(c, "Engineering", 4)` | `Optional.empty` |
| same | `nameAt(c, "Engineering", 0)` or `-1` | `IllegalArgumentException` |
