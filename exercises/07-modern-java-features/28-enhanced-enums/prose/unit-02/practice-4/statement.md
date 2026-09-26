The oldest "generic enum" pattern needs no enum at all: a generic class with
`public static final` instances. Each instance carries its own type argument,
even a parameterized one such as `TypedKey<List<String>>`, which a `Class`
token cannot express. The price, the page says, is everything an enum gives for
free: there is no `values()`, no lookup by name, and the set is closed only
while the constructor stays `private`.

The starter declares the four keys and the accessors. Write the rest:

- The constructor `TypedKey(String name, T defaultValue)` stores both and
  records the new key, so that `values()` can list it. No code outside the class
  may create a key.
- `static List<TypedKey<?>> values()` lists every key in declaration order. The
  caller cannot change the list.
- `static Optional<TypedKey<?>> named(String name)` returns the key whose
  `name()` equals `name`, or empty.

| call | answer |
|---|---|
| `values()` | `[port, host, verbose, allowed.origins]` (as keys) |
| `PORT.defaultValue()` | `8080` (an `Integer`) |
| `ALLOWED_ORIGINS.defaultValue()` | `["*"]` (a `List<String>`) |
| `named("verbose")` | `Optional[VERBOSE]` |
| `named("timeout")` | `Optional.empty` |
| `values().clear()` | throws `UnsupportedOperationException` |

Mind the order of static initialization: a static field is initialized in the
order it is written, and the constants' constructors run while the class is
being initialized.
