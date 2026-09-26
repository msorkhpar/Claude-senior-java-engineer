A string literal lives in the String pool, and every equal literal refers to that one
object. A String built at run time, with `new` or from other Strings, is a separate object
even when its text is the same. `intern()` returns the pool's object for that text, adding
it to the pool if it is not there yet.

Write `canonical(String name)` in `Names`. It returns the pooled String with the same text
as `name`, so that equal names become one shared object:

- `canonical(new String("Hello"))` is the very object the literal `"Hello"` refers to, so
  `canonical(new String("Hello")) == "Hello"` is `true`;
- two equal names built at run time come back as the same object;
- `canonical(null)` is `null`.
