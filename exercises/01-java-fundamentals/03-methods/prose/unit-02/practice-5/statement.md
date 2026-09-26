When a method may have no result, returning `Optional` says so in its type, and the caller
cannot forget the empty case the way it can forget a `null` check. The method also validates
its own parameters instead of failing on a `null`.

Write `longest(List<String> strings)` in `Longest`:

- it returns the longest string: `longest(List.of("a", "abc", "ab"))` is `Optional.of("abc")`;
- when several are equally long, the first of them wins:
  `longest(List.of("one", "two", "six"))` is `Optional.of("one")`;
- `null` elements are ignored;
- an empty list, a list of only `null`s, and a `null` list give `Optional.empty()`.
