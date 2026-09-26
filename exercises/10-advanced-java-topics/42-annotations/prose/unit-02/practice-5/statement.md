Annotations cannot extend each other (Q6). The page's workarounds are
composition: an annotation **annotated with another annotation**, and an
element of type `Class<?>` that names the code to run, as in
`@Constraint(validatedBy = NotEmptyValidator.class)`.

Given: `Check` (`boolean ok(Object value)`), the meta-annotation
`@Constraint(validatedBy)`, the constraints `@NotBlank` and `@Positive` that
carry it, and `@Label`, which does not. Write `Checks.failures(Object bean)`:
for every field the bean's class declares, and every constraint that applies
to it, create the constraint's `validatedBy` class through its no-argument
constructor and call `ok(value)`. Return `"<field>: <ConstraintSimpleName>"`
for each failure, sorted.

- A constraint is an annotation whose type carries `@Constraint`. That works
  for constraints the checker has never heard of.
- **Every constraint on a field is checked**, not only the first.
- **A composed annotation brings its constraints**: a field annotation that is
  not a constraint itself applies each constraint on its annotation type (one
  level deep).

| field | value | failures |
|---|---|---|
| `@NotBlank name` | `" "` | `name: NotBlank` |
| `@Positive @Even count` | `-3` | `count: Even`, `count: Positive` |
| `@Label("Nick") nick` | `null` | none |
| `@Username login` (`@Username` is annotated `@NotBlank`) | `""` | `login: NotBlank` |
