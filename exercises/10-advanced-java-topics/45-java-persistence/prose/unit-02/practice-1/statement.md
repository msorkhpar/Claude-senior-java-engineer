The page's fifth pitfall: `em.find(Employee.class, 1)` twice in one
persistence context gives `e1 == e2`. The `EntityManager` keeps each loaded
entity in its first-level cache, keyed by id, and hands back the same managed
instance. Build that cache by hand.

`ProductSession` is given an open `Connection` to
`products(id INT PRIMARY KEY, name VARCHAR, price DECIMAL(10,2))`.
`find(id)` returns `Optional<Product>`:

- The first `find` of an id loads the row; later calls in the same session
  return the same instance and **run no query**.
- **Ids are compared by value**, so id 1000 is cached like id 7.
- **Each session has its own cache**: a new session loads its own instance.
- An id with no row gives `Optional.empty()`.

| calls | answer | queries run |
|---|---|---|
| `s.find(7)`, `s.find(7)` | the same `Product[7, Pen, 1.50]` twice | 1 |
| `s.find(1000)`, `s.find(1000)` | the same instance twice | 1 |
| `s1.find(7)`, `s2.find(7)` | two different instances | 2 |
