The page's first edge case: in a bidirectional relationship, always maintain
both sides, with a helper such as

```java
public void addProduct(Product p) {
    products.add(p);
    p.setCategory(this);
}
```

That helper still has gaps. Write `Shop.Category` and `Shop.Product` so that
the two sides can never disagree:

- `category.addProduct(p)` adds `p` to the category's products and sets
  `p.category()` to it. **Moving a product removes it from its old
  category**, and **adding the same product twice lists it once**.
- `category.removeProduct(p)` takes `p` out and **clears its category**
  (`p.category()` becomes `null`). Removing a product that is not in the
  category changes nothing.
- `category.products()` returns the products in the order they were added.
  **It hands out no list that edits the category behind `addProduct`'s back.**

Products are compared by identity (two products with the same name are
different products).

| steps | `books.products()` | `pens.products()` | `dune.category()` |
|---|---|---|---|
| `books.addProduct(dune)` | `[dune]` | `[]` | books |
| then `pens.addProduct(dune)` | `[]` | `[dune]` | pens |
| then `pens.removeProduct(dune)` | `[]` | `[]` | `null` |
