The page declares its annotations with `@interface`, and its best practices
say: **always state `@Retention` and `@Target`**, give optional elements
meaningful defaults, use an enum rather than a `String` for a fixed set of
choices, and add `@Documented` to public API annotations. The default
retention is CLASS, which reflection cannot see.

`Kit` already names each annotation and its elements. Add the meta-annotations
and defaults so that every one of them is readable at run time:

| annotation | kind | target | also |
|---|---|---|---|
| `ThreadSafe` | marker, no elements | types | **RUNTIME** |
| `Author` | single `value()` | **methods and types** | RUNTIME |
| `ApiEndpoint` | multi-value | methods | RUNTIME, `@Documented`; `path` required; `method` `GET`, `description` `""`, `version` `1`, **`produces` `{"application/json"}`** |
| `Role` | repeatable `value()` | methods and types | RUNTIME, **`@Repeatable(Roles.class)`** |
| `Roles` | container, `Role[] value()` | methods and types | RUNTIME |
| `Auditable` | `level()` | types | RUNTIME, **`@Inherited`**, `level` `"INFO"` |
