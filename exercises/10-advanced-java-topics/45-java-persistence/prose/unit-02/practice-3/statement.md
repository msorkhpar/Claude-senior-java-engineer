`@OneToMany` is LAZY by default: the collection is fetched the first time it
is used. The page's third pitfall touches it after the `EntityManager` is
closed, which throws `LazyInitializationException`. Build that behaviour by
hand.

`Catalog` is given an open `Connection` to `categories(id, name)` and
`products(id, name, category_id)`:

- `category(id)` returns `Optional<Category>` with its id and name. **The
  products are not loaded until `products()` is first called.**
- `Category.products()` returns the product names, ordered by id. **They are
  queried once, then kept.**
- `close()` closes the catalog (not the connection, which is the caller's).
  **Loading after close throws `Catalog.LazyInitializationException`**, but
  **products loaded before close stay readable after it**.

| steps | result |
|---|---|
| `category(1)` | `Books`, 1 query so far |
| `products()`, `products()` | `[Dune, Emma]` both times, 2 queries in all |
| `category(1)`, `close()`, `products()` | `LazyInitializationException` |
| `category(1)`, `products()`, `close()`, `products()` | `[Dune, Emma]` |
