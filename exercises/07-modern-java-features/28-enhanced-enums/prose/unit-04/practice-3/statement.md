A bounded type parameter lets an enum method accept any type that has what it
needs, and nothing else:

```java
public abstract <T extends Number> double aggregate(List<T> values);
```

`Aggregator.SUM.aggregate(List.of(1, 2, 3))` compiles; a `List<String>` does
not. Inside, the bound unlocks `Number`'s methods, and the page reads every
value with `doubleValue()`. For an empty list, the page's rule is that `SUM`
and `AVERAGE` give `0.0`, while `MIN` and `MAX` throw
`NoSuchElementException`.

Fill in each constant's `aggregate`: `SUM`, `AVERAGE`, `MIN`, `MAX`, and
`COUNT` (how many values there are).

| call | answer |
|---|---|
| `SUM.aggregate(List.of(1, 2, 3))` | `6.0` |
| `AVERAGE.aggregate(List.of(1.5, 2.5))` | `2.0` |
| `MAX.aggregate(List.of(100L, 200L))` | `200.0` |
| `SUM.aggregate(List.<Number>of(1, 2.5, 3L))` | `6.5` |
| `AVERAGE.aggregate(List.of())` | `0.0` |
| `MIN.aggregate(List.of())` | throws `NoSuchElementException` |
