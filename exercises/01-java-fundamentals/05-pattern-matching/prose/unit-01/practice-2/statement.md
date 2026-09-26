A cast to the wrong type throws `ClassCastException`, so a value of unknown type is tested
with `instanceof` first, and cast only when the test passes:

```java
if (obj instanceof String) {
    String str = (String) obj;
    ...
}
```

`instanceof` is `false` for `null`, so the same test also skips `null`.

Write `sum(List<Object> items)` in `Numbers`. It adds up every item that is a `Number`
(`Integer`, `Long`, `Double`, ...), as a `double`, and ignores every other item:

- `sum(List.of(1, 2.5, 3L))` is `6.5`;
- `sum(List.of(1, "two", 3))` is `4.0`;
- `null` items are ignored: `sum(Arrays.asList(1, null, 2))` is `3.0`.
