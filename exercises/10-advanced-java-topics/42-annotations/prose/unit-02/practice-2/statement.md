When `@Role` is written twice on a method, the compiler wraps both in the
container `@Roles`. The page's Q4 warns that a single `@Role` is **not
wrapped**: then `getAnnotation(Roles.class)` is `null` and only
`getAnnotation(Role.class)` finds it. `getAnnotationsByType(Role.class)`
handles both cases.

`Role` and `Roles` are given. Write:

- `rolesOf(AnnotatedElement e)`: the role names on `e`, in the order written,
  whether there is one `@Role` or several.
- `canCall(Method m, Set<String> granted)`: `true` when `granted` holds at
  least one of `m`'s roles. **A method without roles is open to everyone.**
  Role names are **compared by value**.

| method | `rolesOf` | `canCall` with `{MANAGER}` | with `{}` |
|---|---|---|---|
| `@Role("ADMIN") @Role("MANAGER") listUsers()` | `[ADMIN, MANAGER]` | `true` | `false` |
| `@Role("ADMIN") createUser()` | `[ADMIN]` | `false` | `false` |
| `health()` | `[]` | `true` | `true` |
