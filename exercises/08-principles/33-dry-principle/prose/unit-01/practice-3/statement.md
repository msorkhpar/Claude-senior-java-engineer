The page's **Template Method** defines the skeleton of an algorithm once, in a base class,
and lets each subclass fill in only the steps that vary:

```java
public abstract class DataProcessor<T, R> {
    // Template method -- defines the algorithm once
    public final List<R> process(List<T> items) { ... }
    protected abstract boolean isValid(T item);
    protected abstract R transform(T item);
}
```

Complete `DataProcessor` and its two processors:

- `process(items)` is the one pipeline: it keeps every item `isValid` accepts (nothing
  else decides, not even for a `null` item), in order and with repeats kept, and returns
  what `transform` makes of each. It is `final`, and a subclass writes only `isValid` and
  `transform`, so a processor written tomorrow gets the same pipeline with no copy of it.
  A `null` list throws `NullPointerException`;
- `UpperCase` keeps the strings that are neither `null` nor blank, in upper case and
  otherwise unchanged (no trimming: `" hi "` becomes `" HI "`). Use
  `Locale.ROOT`, so the result is the same on every machine (under a Turkish default
  locale, a plain `toUpperCase()` turns `"title"` into `"TİTLE"`);
- `Doubler` keeps the positive integers, doubled.

Examples:

- `new DataProcessor.UpperCase().process(Arrays.asList("hello", "", null, "  ", "world"))`
  is `["HELLO", "WORLD"]`;
- `new DataProcessor.Doubler().process(Arrays.asList(1, -2, 0, 3, null, 200))` is
  `[2, 6, 400]`, and `[1000, 1000, 7]` gives `[2000, 2000, 14]`;
- an empty list gives an empty list;
- a processor that keeps the items starting with `"#"` and transforms each to its length
  less one turns `["#java", "plain", "#dry"]` into `[4, 3]`, with no `process` of its own;
- a processor whose `isValid` accepts everything and whose `transform` is
  `String.valueOf` turns `[null, "a"]` into `["null", "a"]`.
