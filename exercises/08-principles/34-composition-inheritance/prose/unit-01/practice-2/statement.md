Composition does not need a class per behaviour: a class can hold functional interfaces and
combine them at run time. The page's `TextProcessor` holds a list of
`Function<String, String>` and applies them one after another:

```java
var processor = new TextProcessor()
    .addTransformation(String::trim)
    .addTransformation(String::toUpperCase)
    .addTransformation(s -> s.replace(" ", "_"));

processor.process("  hello world  "); // "HELLO_WORLD"
```

Write `addTransformation` and `process` in `TextProcessor`:

- `addTransformation(t)` adds `t` after the transformations already added, and returns the
  same processor every time, so calls chain; trim then upper-case turns `"  hello  "` into `"HELLO"`;
- `process(input)` applies the transformations **in the order they were added**: the
  example above gives `"HELLO_WORLD"`. A transformation added twice runs twice, in each of
  its places;
- a processor with no transformations returns its input unchanged;
- the composition is live: a transformation added after `process` has run is used by the
  next `process` call.
