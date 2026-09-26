`@Deprecated` has RUNTIME retention, so a tool can list at run time what a
class has deprecated and how urgently. Write
`DeprecationReport.of(Class<?> type)`, one line per deprecated method or
constructor **the class itself declares**, sorted:

- the signature: the method name, or for a constructor the class's simple
  name, then the parameter types' simple names in parentheses, joined with
  `", "`;
- then `" since <version>"` when `since` is set (**an empty `since` is left
  out**);
- then `", for removal"` when `forRemoval` is true.

Deprecated **constructors are listed too**. Members a class inherits are not
its own and are not listed.

| class | report |
|---|---|
| the page's `LegacyApi` | `[legacyCalculation(int, int) since 1.5, oldMethod() since 2.0, for removal]` |
| `Connection` with `@Deprecated(since = "3.0") Connection(String url)` | `[Connection(String) since 3.0]` |
| `Job` with plain `@Deprecated void run()` | `[run()]` |
| `class Modern extends LegacyApi`, nothing deprecated | `[]` |
