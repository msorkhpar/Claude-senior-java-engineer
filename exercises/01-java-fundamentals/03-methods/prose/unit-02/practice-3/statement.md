A method returns one value. To return two, return one object that carries both: since
Java 16 a record does it in one line, `record MinMax(int min, int max) {}`.

`Range` gives you that record. Write `of(int[] numbers)` in `Range`. It returns the smallest
and the largest value together, as an `Optional<MinMax>`:

- `of(new int[]{4, -2, 9, 3})` is `Optional.of(new MinMax(-2, 9))`;
- a single value is both: `of(new int[]{7})` is `MinMax(7, 7)`;
- an empty or `null` array has no smallest or largest value: return `Optional.empty()`,
  not a made-up pair such as `MinMax(Integer.MAX_VALUE, Integer.MIN_VALUE)`.
