Overloading is resolved at compile time, from the static types of the arguments, not from
the objects they hold at run time. With `label(Object)` and `label(String)`:

- `label("hi")` calls `label(String)`;
- `Object o = "hi"; label(o)` calls `label(Object)`, because `o` is declared `Object`;
- `label(null)` calls `label(String)`, the most specific overload that fits.

Write both overloads in `Labels`:

1. `label(Object obj)` returns `"object: "` followed by the object: `label(42)` is
   `"object: 42"`;
2. `label(String text)` returns `"text of "` followed by the length: `label("hi")` is
   `"text of 2"`, and for `null` it returns `"no text"`.

Do not look at the run-time class inside `label(Object)`: a `String` that arrives there was
declared as an `Object` by its caller, and is labelled as an object.
