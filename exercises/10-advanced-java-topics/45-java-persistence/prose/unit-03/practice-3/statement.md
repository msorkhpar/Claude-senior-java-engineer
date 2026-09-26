The page's Q4 imports a list in one transaction and sets a savepoint per
record, so a bad record is rolled back alone with `rollback(savepoint)` and
the rest are committed together at the end.

Write `Importer.importAll(connection, people)`. Each `Person(id, name, email)`
takes two inserts: `people(id, name)` and then `emails(person_id, email)`,
where `email` is UNIQUE. It returns the ids imported, in input order.

- Turn autoCommit off, and commit once at the end.
- **A failure rolls back to that person's own savepoint, keeping the people
  before it**; that person is skipped.
- **The savepoint is set before the person's first insert**, so none of a
  failed person remains.
- **`autoCommit` is restored** when the import ends.

| people (id, name, email) | returned | rows in people |
|---|---|---|
| (1, Ada, ada@example.org), (2, Linus, linus@example.org), (3, Grace, grace@example.org) | `[1, 2, 3]` | 1, 2, 3 |
| (1, Ada, ada@example.org), (2, Linus, ada@example.org), (3, Grace, grace@example.org) | `[1, 3]` | 1, 3 |
