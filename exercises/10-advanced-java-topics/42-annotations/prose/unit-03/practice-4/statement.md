Reflection cannot see an annotation on a local variable, but it can see a
RUNTIME annotation on a **method parameter**: `Method.getParameterAnnotations()`
returns one array per parameter, holding every annotation on it. The page's
`@RequestParam(name, required = true)` is the kind of parameter annotation a
web framework reads to fill in a handler's arguments.

`@RequestParam` is given. Write `Binder.bind(Method m, Map<String, String> query)`,
which returns the arguments for `m`, one per parameter, in order:

- each parameter's `@RequestParam` names its query key. A parameter can carry
  **other annotations too**; find `@RequestParam` among them. A parameter with
  none is refused with `IllegalStateException`.
- `String` parameters get the value as it is, `int` parameters get it parsed.
- **A missing required value is refused** with `IllegalArgumentException`.
  **A missing optional value binds a default**: `null` for `String`, `0` for
  `int`.

| handler | query | arguments |
|---|---|---|
| `search(@RequestParam(name = "q") String q, @RequestParam(name = "page", required = false) int page)` | `{q=java, page=2}` | `[java, 2]` |
| the same | `{q=java}` | `[java, 0]` |
| the same | `{page=2}` | `IllegalArgumentException` |
| `find(@Trimmed @RequestParam(name = "id") String id)` | `{id=42}` | `[42]` |
