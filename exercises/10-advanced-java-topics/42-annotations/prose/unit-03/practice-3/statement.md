The page warns about two lookups that look alike. On a class,
`getAnnotation()` also returns an `@Inherited` annotation from a superclass,
while `getDeclaredAnnotation()` returns only what the class itself declares.
On a method there is no inheritance at all: **an overriding method does not
inherit its annotations**, so each override must declare its own.

`@Transactional(readOnly)` is given; it is `@Inherited` and targets types
and methods. Write `Tx.mode(Class<?> type, String method)` for the public
no-argument method that `type.getMethod(method)` finds:

- if that method carries `@Transactional`, its mode;
- otherwise, if `type` carries `@Transactional`, **its own or inherited from
  a superclass**, the class's mode;
- otherwise empty.

A mode is `"read-only"` or `"read-write"`.

| class | method | mode |
|---|---|---|
| `@Transactional(readOnly = true) Repo` | `save()`, annotated `@Transactional` | `read-write` |
| `Repo` | `find()`, not annotated | `read-only` |
| `CachedRepo extends Repo`, overrides `find()` | `find()` | `read-only` (inherited from `Repo`) |
| `Plain`, no class annotation | `save()`, annotated `@Transactional` | `read-write` |
| `SubPlain extends Plain`, overrides `save()` | `save()` | empty |
