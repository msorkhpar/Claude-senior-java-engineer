`enum DataConverter<T>` is illegal, but a method of an enum may declare its
own type parameter: `<T> T convert(Object input, Class<T> targetType)`. The
type is chosen at each call, so one constant serves `Integer`, `Long` and
`Double` callers alike. The page's pitfall is `return (T) obj;`: erasure makes
that cast do nothing, so a wrong type surfaces later, elsewhere. Casting with
the `Class<T>` token fails at once.

Fill in each constant's `convert`, and write `safeConvert`:

- `STRING_TO_NUMBER`: parse `String.valueOf(input)` as the target type, one of
  `Integer`, `Long`, `Double` or `Float`. Any other target throws
  `UnsupportedOperationException`.
- `TO_STRING`: `String.valueOf(input)`; a target other than `String` throws
  `UnsupportedOperationException`.
- `IDENTITY`: `input` itself, checked with `targetType`: a value of another type
  throws `ClassCastException` inside `convert`.
- `<T> Optional<T> safeConvert(Object input, Class<T> targetType)`: the
  conversion, or an empty `Optional` if the conversion fails for any reason.

| call | answer |
|---|---|
| `STRING_TO_NUMBER.convert("42", Integer.class)` | `42` |
| `STRING_TO_NUMBER.convert("10000000000", Long.class)` | `10000000000L` |
| `TO_STRING.convert(42, String.class)` | `"42"` |
| `IDENTITY.convert("hello", Integer.class)` | throws `ClassCastException` |
| `STRING_TO_NUMBER.convert("1", Boolean.class)` | throws `UnsupportedOperationException` |
| `STRING_TO_NUMBER.safeConvert("abc", Integer.class)` | `Optional.empty` |
