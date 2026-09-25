On two `int`s, `/` truncates toward zero and `%` takes the sign of the dividend:
`-1 / 10` is `0`, and `-1 % 5` is `-1`. That is right for some jobs and wrong for
others. `Math.floorDiv` and `Math.floorMod` round toward negative infinity instead.

Write two methods in `FloorArithmetic` (`size` and `width` are always positive):

1. `wrap(int index, int size)` returns `index` wrapped into `0..size-1`, as on a
   circular buffer: `wrap(7, 5)` is `2`, and a negative index counts back from
   the end, so `wrap(-1, 5)` is `4`.
2. `bucket(int value, int width)` returns which bucket of `width` values `value`
   falls in, where bucket `0` is `0..width-1`, bucket `1` is `width..2*width-1`,
   and bucket `-1` is `-width..-1`: `bucket(25, 10)` is `2` and `bucket(-1, 10)` is `-1`.
