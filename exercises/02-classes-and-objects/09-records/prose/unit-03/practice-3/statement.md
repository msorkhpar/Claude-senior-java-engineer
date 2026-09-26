Java methods return one value. When a method has several results that belong
together, a small **tuple-like record** carries them as one value, with a name
for each part and `equals` and `toString` for free.

`Stats` declares

```java
public record MinMax(int min, int max) {
    long spread();   // max - min
}
```

Write `MinMax.spread()`, and write `static MinMax minMax(int[] values)`, which
returns the smallest and the largest of the values in one pass.

- An empty array is refused with an `IllegalArgumentException`.
- `spread()` is `max - min`, and it may be larger than any `int`.

## Examples

```
minMax(new int[] {3, 1, 4, 1, 5})                    -> MinMax[min=1, max=5], spread() = 4
minMax(new int[] {7})                                -> MinMax[min=7, max=7], spread() = 0
minMax(new int[] {-8, -3, -5})                       -> MinMax[min=-8, max=-3]
minMax(new int[] {})                                 -> IllegalArgumentException
minMax(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}).spread()  -> 4294967295
```
