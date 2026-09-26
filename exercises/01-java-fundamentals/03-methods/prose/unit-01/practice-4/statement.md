A method that cannot handle `null` or an empty collection checks for them first, and
answers them on purpose instead of failing halfway.

Write `summary(List<String> items)` in `Summary`:

- `summary(List.of("a", "b", "c"))` is `"3 items: a, b, c"`;
- one item is singular: `summary(List.of("pen"))` is `"1 item: pen"`;
- `null` and an empty list are both `"nothing to process"`.
