`skip(n)` drops the first `n` elements and `limit(n)` keeps at most `n`. Together they
cut a slice out of a stream, which is exactly what pagination needs. Both take a
`long`.

Write `Pager.page(List<Integer> items, int page, int size)`. Pages are numbered from
`0` and hold `size` items each; return the items of page `page`, in order. `page` is
never negative and `size` is at least `1`.

| items | page | size | answer |
|---|---|---|---|
| `1..10` | `0` | `3` | `[1, 2, 3]` |
| `1..10` | `1` | `3` | `[4, 5, 6]` |
| `1..10` | `3` | `3` | `[10]` |

The list does not always divide into whole pages, a caller may ask past the end, and
`page * size` in `int` arithmetic can overflow.
