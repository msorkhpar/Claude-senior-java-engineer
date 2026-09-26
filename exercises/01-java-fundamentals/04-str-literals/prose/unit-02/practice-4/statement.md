For text with a fixed shape, `String.format` is clearer than a chain of `+`, and its
patterns do what `+` cannot, such as padding a number with zeros: `String.format("%02d", 5)`
is `"05"`.

Write `format(String name, long cents)` in `Money`. `cents` is never negative. It returns the
name, a colon, and the amount in dollars with exactly two digits of cents:

- `format("Ada", 1250)` is `"Ada: $12.50"`;
- `format("Ada", 1205)` is `"Ada: $12.05"`;
- `format("Bob", 7)` is `"Bob: $0.07"`, and `format("Cy", 300)` is `"Cy: $3.00"`.
