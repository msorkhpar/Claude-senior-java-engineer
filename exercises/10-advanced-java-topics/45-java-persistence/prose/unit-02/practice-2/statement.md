The page asks "How does dirty checking work in Hibernate?" and answers:
snapshot comparison at flush time. When an entity is loaded, the session
keeps a copy of its state; `flush()` compares each managed entity with its
snapshot and writes an UPDATE only for those that changed. Nobody calls
`update()`.

`UnitOfWork` is given an open `Connection` to
`products(id INT PRIMARY KEY, name VARCHAR, price DECIMAL(10,2))`.

- `load(id)` reads the row, returns a mutable `Product` and keeps a snapshot
  of its name and price. An unknown id throws `IllegalArgumentException`.
  (Each test loads an id at most once.)
- `flush()` runs `UPDATE products SET name = ?, price = ? WHERE id = ?`
  **only for products whose state differs from the snapshot**, and returns
  how many it wrote. **Names are compared with `equals()`** and **prices with
  `compareTo()`**, so `1.5` equals `1.50`.
- **`flush()` refreshes the snapshot**, so a second flush with no new change
  writes nothing.

| loaded | change | `flush()` |
|---|---|---|
| 1 (Pen, 1.50) | `setPrice(2.25)` | `1` |
| 1 (Pen, 1.50), 2 (Ink, 4.00) | product 1 only: `setPrice(2.25)` | `1` |
| 1 (Pen, 1.50) | `setName(new String("Pen"))` | `0` |
| 1 (Pen, 1.50) | `setPrice(1.5)` | `0` |
| 1 (Pen, 1.50) | `setPrice(2.25)`, `flush()`, then `flush()` again | `1`, then `0` |
