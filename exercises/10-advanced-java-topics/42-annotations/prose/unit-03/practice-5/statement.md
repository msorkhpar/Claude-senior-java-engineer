A `TYPE_USE` annotation annotates a **type**, not a declaration: in
`@NonNull String name` the annotation belongs to the type `String`, and in
`List<@NonNull String> tags` it belongs to the type argument. With RUNTIME
retention the page says it is reachable through `AnnotatedType` objects:
`field.getAnnotatedType()`, and for a parameterized type,
`AnnotatedParameterizedType.getAnnotatedActualTypeArguments()`.
`field.getAnnotation(NonNull.class)` does not find it.

`@NonNull` (`TYPE_USE`, RUNTIME) is given. Write
`NullCheck.problems(Object bean)`, over the fields the bean's class declares,
sorted:

- `"<field> is null"` when the field's type is `@NonNull` and its value is
  `null`;
- `"<field> has a null element"` when the field is a collection whose **type
  argument is `@NonNull`** and it holds a `null`.

**Only annotated types are checked**: a plain `String` may be `null` and a
plain `List<String>` may hold `null`s.

| `Profile` fields | values | problems |
|---|---|---|
| `@NonNull String name`, `List<@NonNull String> tags`, `List<String> notes`, `String nickname` | `"Ada"`, `[java]`, `[]`, `"ada"` | `[]` |
| the same | `name = null` | `[name is null]` |
| the same | `tags = [java, null]` | `[tags has a null element]` |
| the same | `notes = [null]`, `nickname = null` | `[]` |
