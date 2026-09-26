The page's Q1 simulates Spring's field injection: every field that carries
`@Inject` gets a value from a registry, looked up by a key. `@Inject("dataSource")`
names the key; a bare `@Inject` has `value()` equal to `""`, and then **the
field's own name is the key**. Private fields are injected too.

`KeyedInjector.Inject` (given) has `String value() default ""`. Write
`KeyedInjector.inject(Object target, Map<String, Object> registry)`:

- For each field that `target`'s class declares with `@Inject`, compute the
  key and, when the registry holds a value for it, set the field to that value.
- **A key the registry does not hold leaves the field as it was**: a field
  that already had a value keeps it.
- **Fields without `@Inject` are never touched**, even when the registry holds
  an entry under their name.

| field | registry | after `inject` |
|---|---|---|
| `@Inject("dataSource") private String dataSource` | `dataSource -> "jdbc:mysql://localhost/db"` | `"jdbc:mysql://localhost/db"` |
| `@Inject private String config` | `config -> "production"` | `"production"` |
| `@Inject private String region = "eu"` | no `region` entry, or `region -> null` | `"eu"` |
| `@Inject("pool") private Integer poolSize` | `pool -> 1024`, `poolSize -> 1` | `1024` |
| `private String secret = "kept"` (no `@Inject`) | `secret -> "leaked"` | `"kept"` |
