Leaving out `break` makes execution fall through into the next case's statements. That is
usually a bug, but it can be used on purpose, when each case should also do everything
the cases below it do.

In the carol, day `n` lists the gifts of day `n` and of every earlier day, newest first.
Write `gifts(int day)` in `Carol` for days `1` to `4`, with one `switch` whose cases fall
through:

| day | gifts |
|---|---|
| 1 | `a partridge in a pear tree` |
| 2 | `two turtle doves`, `and a partridge in a pear tree` |
| 3 | `three French hens`, `two turtle doves`, `and a partridge in a pear tree` |
| 4 | `four calling birds`, `three French hens`, `two turtle doves`, `and a partridge in a pear tree` |

On day 1 the partridge has no "and". A day outside `1` to `4` throws
`IllegalArgumentException`.
