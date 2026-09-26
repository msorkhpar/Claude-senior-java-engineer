The page's edge cases say that `reduce(identity, op)` returns the identity for
an empty stream, while `reduce(op)` without an identity returns an `Optional`,
because an empty stream has no answer. Its source groups with
`Collectors.partitioningBy`, which splits a stream by a predicate.

Write `ScoreReport.report(List<Integer> scores)`. A score passes when it is
at least `ScoreReport.PASS_MARK` (`50`). Return one line:

```
count=<n> total=<sum> best=<highest or none> passed=<list> failed=<list>
```

- `total` is `0` when there are no scores, and `best` is `none`. With scores,
  `best` is the highest one, even if that is `0` or negative.
- `passed` and `failed` keep the scores in their **original order** (not sorted) and are printed
  the way `List.toString()` prints them; a side with no scores prints `[]`.

| scores | answer |
|---|---|
| `[60, 40, 90, 100]` | `count=4 total=290 best=100 passed=[60, 90, 100] failed=[40]` |
| `[70, 20, 55]` | `count=3 total=145 best=70 passed=[70, 55] failed=[20]` |
| `[10, 20]` | `count=2 total=30 best=20 passed=[] failed=[10, 20]` |
| `[]` | `count=0 total=0 best=none passed=[] failed=[]` |
