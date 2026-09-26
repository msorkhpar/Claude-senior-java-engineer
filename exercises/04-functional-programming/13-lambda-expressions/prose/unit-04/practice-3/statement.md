Three levels of nested `stream().map(...reduce...)` inside one lambda are hard to read. The
page's cure is a small method for each level, each pipeline calling the next one by name, with
`mapToDouble(...).sum()` for the sums. That shape is advice; the tests check the totals.

The file declares `enum Status { PENDING, COMPLETED }`, `record Item(double price)`,
`record Order(Status status, List<Item> items)` and `record Customer(String name, List<Order> orders)`.
Write four static methods of `Totals`:

1. `double orderTotal(Order order)`: the sum of the order's item prices.
2. `double customerTotal(Customer customer)`: the sum of the customer's order totals.
3. `List<Double> customerTotals(List<Customer> customers)`: each customer's total, in order.
4. `double completedRevenue(List<Order> orders)`: the sum of item prices over `COMPLETED`
   orders only, counting only prices above zero.

| call | answer |
|---|---|
| `orderTotal(Order(COMPLETED, [10.0, 2.5]))` | `12.5` |
| `customerTotal(Customer("Ann", [that order, Order(PENDING, [7.5])]))` | `20.0` |
| `completedRevenue([Order(COMPLETED, [10.0, 2.5]), Order(COMPLETED, [4.0])])` | `16.5` |

An order can be empty and so can a customer's order list; `completedRevenue` has two filters,
not one.
