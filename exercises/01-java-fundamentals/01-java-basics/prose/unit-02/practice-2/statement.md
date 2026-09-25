`==` on two references asks whether they point at **the same object**, not
whether the objects hold the same value. `Integer.valueOf` returns a cached
object for values from -128 to 127, so `==` on two `Integer`s often looks right
in a quick test and then fails on bigger numbers. And a reference can be `null`,
where calling a method on it throws a `NullPointerException`.

Write `BoxedEquality.sameValue(Integer a, Integer b)`. It returns `true` when
both hold the same number, or when both are `null`; otherwise `false`.

| a | b | answer |
|---|---|---|
| `5` | `5` | `true` |
| `1000` | `1000` | `true` |
| `null` | `null` | `true` |
| `null` | `5` | `false` |
