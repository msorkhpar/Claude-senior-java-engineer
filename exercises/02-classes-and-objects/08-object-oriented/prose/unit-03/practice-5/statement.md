An abstraction shows what an object does and hides how. A **leaky
abstraction** exposes its implementation, for example by handing callers the
very collection it keeps its state in, so they can change that state behind
its back.

Write `Inventory`, which keeps stock counts by item name however you like, and
exposes only these methods:

- `void add(String item, int quantity)`: adds stock; a quantity of 0 or less
  throws `IllegalArgumentException`.
- `boolean remove(String item, int quantity)`: takes stock away and returns
  `true`; when there is not that much in stock it returns `false` and changes
  nothing. An item whose count reaches 0 leaves the inventory.
- `int count(String item)`: the stock of an item, `0` for an item never added.
- `Map<String, Integer> items()`: what is in stock, item to count. Nothing a
  caller does with the returned map may change the inventory.

## Examples

```
add("apple", 3); add("pear", 2); add("apple", 2)
count("apple")        -> 5
remove("apple", 4)    -> true,  count("apple") -> 1
items()               -> {apple=1, pear=2}
count("kiwi")         -> 0
remove("pear", 10)    -> false, count("pear") -> 2
remove("pear", 2)     -> true,  items() -> {apple=1}
items().put("apple", 100)  -> the inventory still counts 1 apple
```
