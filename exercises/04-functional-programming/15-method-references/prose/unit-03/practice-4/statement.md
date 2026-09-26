`Optional`'s methods take functional interfaces, so method references fit them:
`map(String::length)`, `filter(...)`, `ifPresent(...)`. A reference that itself returns
an `Optional` belongs in `flatMap`, or the result nests as `Optional<Optional<T>>`. A
`Supplier` such as `ArrayList::new` defers its work until `get()` is called.

`Member` is a record `(String name, String nick)` whose `nickname()` method returns
`Optional<String>` (empty when `nick` is `null`). Write three methods in `Lookups`:

1. `Optional<String> nickname(Optional<Member> member)` returns the member's nickname
   upper-cased, or empty.
2. `Optional<Integer> trimmedLength(Optional<String> text)` returns the length of the
   trimmed text, or empty when there is no text or nothing is left after trimming.
3. `<T> T orCreate(Optional<T> value, Supplier<T> factory)` returns the value, or a new
   one from `factory` when it is missing.

| call | result |
|---|---|
| `nickname(Optional.of(new Member("Robert", "bob")))` | `Optional[BOB]` |
| `trimmedLength(Optional.of("  hi "))` | `Optional[2]` |
| `orCreate(Optional.empty(), ArrayList::new)` | a new empty list |

Some members have no nickname, some text is only spaces, and a factory may be costly to
call.
