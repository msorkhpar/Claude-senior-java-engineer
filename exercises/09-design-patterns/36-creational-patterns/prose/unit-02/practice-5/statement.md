Every new product in the classic pattern needs a new creator subclass, a
parallel hierarchy. The page's modern alternative is a **`Supplier` per
product**, registered under a name, plus a **generic** `create(Class<T>)`-style
method that hands back a typed product.

Write `ProductRegistry`:

- `register(String name, Supplier<?> factory)` stores the factory. A name
  already taken throws `IllegalStateException` and keeps the first one.
- `create(String name)` runs that name's factory and returns **a new product on
  every call**. An unknown name throws `IllegalArgumentException` whose message
  contains the name.
- `create(String name, Class<T> type)` does the same and returns the product as
  a `T`; a product that is not a `T` throws `IllegalArgumentException`.

| registered | call | result |
|---|---|---|
| `"list" -> ArrayList::new` | `create("list")` | an empty `ArrayList`, a new one each time |
| `"list" -> ArrayList::new` | `create("list", List.class)` | a `List` |
| `"list" -> ArrayList::new` | `create("list", Map.class)` | `IllegalArgumentException` |
| nothing under `"set"` | `create("set")` | `IllegalArgumentException` |
