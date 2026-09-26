The page's fifth pitfall: `volatile Config config` makes only the **reference**
volatile. Editing `config.timeout` in place is not published to other threads, and a
reader can see a half-changed config. The fix is the immutable update pattern: `Config`
has only `final` fields, and an update **replaces the whole object** through the
`volatile` reference. The page's final-field rule is what makes such an object safe to
read without a lock.

Write the two classes:

- `Server.Config`, holding a `timeout` (an `int`) and a `host` (a `String`), with the
  accessors `timeout()` and `host()`;
- `Server`, built with a starting timeout and host, with `config()` returning the
  current `Config`, and `withTimeout(int)` and `withHost(String)` each changing one
  setting and keeping the other.

One admin thread makes the updates; many threads call `config()`.

| calls | answer |
|---|---|
| `new Server(30, "localhost").config()` | timeout `30`, host `"localhost"` |
| then `withTimeout(60)`, `config()` | timeout `60`, host `"localhost"` |
| a `Config` read before the update | still timeout `30` |
