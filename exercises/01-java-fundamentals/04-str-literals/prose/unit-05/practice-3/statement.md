`append` returns the builder itself, so calls chain: `sb.append("a").append("b")`. And
`append` never refuses a `null` reference: it adds the four letters `"null"`. A value that
may be `null` is checked before it is appended. `toString()` turns the finished builder into
a String.

Write `trail(String... parts)` in `Breadcrumbs`. It returns the parts separated by `" / "`,
leaving out every `null` part:

- `trail("Home", "Docs", "Java")` is `"Home / Docs / Java"`;
- `trail("Home", null, "Java")` is `"Home / Java"`;
- `trail()` and `trail((String) null)` are `""`.
