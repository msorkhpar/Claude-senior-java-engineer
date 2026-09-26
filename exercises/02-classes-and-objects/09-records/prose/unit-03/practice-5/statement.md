Records fit **functional-style** code: small immutable values flowing through a
stream, grouped, summarised and sorted by their components.

`Orders` declares two records:

```java
record Order(String customer, long cents) {}
record CustomerTotal(String customer, long totalCents, int orders) {}
```

Write `static List<CustomerTotal> totals(List<Order> orders)`, which returns
one `CustomerTotal` per customer: the sum of their orders' cents and how many
orders they placed. The list is sorted by `totalCents`, largest first; customers
with equal totals are sorted by name, A to Z. The input is not changed.

## Examples

```
totals([Order(ann, 500), Order(bob, 300), Order(ann, 250)])
    -> [CustomerTotal[customer=ann, totalCents=750, orders=2],
        CustomerTotal[customer=bob, totalCents=300, orders=1]]

totals([Order(cid, 100), Order(ann, 100)])
    -> [CustomerTotal[customer=ann, totalCents=100, orders=1],
        CustomerTotal[customer=cid, totalCents=100, orders=1]]

totals([]) -> []
```
