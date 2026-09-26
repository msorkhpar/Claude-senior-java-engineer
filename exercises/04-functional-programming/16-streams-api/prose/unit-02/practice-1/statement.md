`Stream.of((String) null)` is **not** an empty stream: it holds one `null` element.
`Stream.ofNullable(value)` (Java 9) is the null-safe tap: empty for `null`, a
one-element stream otherwise. And `Arrays.stream(array)` on a `null` array throws,
so a source that may be `null` must be guarded before it is streamed.

Write two methods in `NullSafe`:

1. `nicknames(List<NullSafe.Person> people)` returns the nicknames of the people, in
   their order. A `Person(String name, String nickname)` may have a `null` nickname;
   such a person contributes nothing.
   Every other nickname is listed, even an empty one, and a repeated one each time.
2. `countItems(String[] items)` returns how many slots the array has, `null`
   entries included; a `null` array holds none.

| call | answer |
|---|---|
| `nicknames([Person("Robert", "Bob"), Person("Katherine", "Kate")])` | `["Bob", "Kate"]` |
| `nicknames([Person("Robert", "Bob"), Person("Ann", null)])` | `["Bob"]` |
| `countItems(["a", "b", "c"])` | `3` |
| `countItems(null)` | `0` |

Neither method throws on a `null`, and neither lets one into its answer.
