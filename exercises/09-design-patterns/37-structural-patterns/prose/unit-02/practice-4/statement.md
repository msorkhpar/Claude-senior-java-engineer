The page's Q4: when the component is a functional interface, a decorator can
be a function wrapped around another, built with `Function.andThen`. Its
example trims, then upper-cases, then adds a `"[LOG] "` prefix.

Write `Pipelines.decorate(base, layers)`, returning a `UnaryOperator<String>`
that runs `base` first and then each layer on the result:

- **layers run in list order**: the first layer wraps the base, the next wraps
  that, and so on;
- decoration is cumulative, so **a layer listed twice runs twice**;
- an empty list leaves the base alone.

| base | layers | input | output |
|---|---|---|---|
| `String::trim` | `[toUpperCase, s -> "[LOG] " + s]` | `"  hello  "` | `"[LOG] HELLO"` |
| `String::trim` | `[]` | `"  hello  "` | `"hello"` |
| identity | `[s -> s + "!", s -> s + s]` | `"hello"` | `"hello!hello!"` |
| identity | `[s -> s + s, s -> s + "!"]` | `"hello"` | `"hellohello!"` |
| identity | `[bang, bang]` (one object) | `"hello"` | `"hello!!"` |
