The N+1 select problem: one query loads N categories, then touching each
category's products runs one more query per category. The page's fix is
`JOIN FETCH`: one query with a join, whose rows are then grouped back into
parents (the page's Q3 warns that a join repeats each parent once per child).

Write `CategoryReport.load(connection)` over `categories(id, name)` and
`products(id, name, category_id)`. It returns a `List<CategoryProducts>`,
with categories in id order and each one's product names in id order.

- **One joined query** loads every category and product, never one query per
  category.
- **The join keeps categories without products** (an inner join would drop
  them), and such a category has **an empty list, not `[null]`**.

| categories | products (category) | `load(c)` |
|---|---|---|
| 1 Books, 2 Pens, 3 Toys | Dune (1), Biro (2), Emma (1), Kite (3) | `[Books [Dune, Emma], Pens [Biro], Toys [Kite]]` |
| same, plus 4 Maps | same | `[..., Maps []]` |
