The page's Q4 builds method-level security from an annotation and a dynamic
proxy, the way Spring Security's `@Secured` works. The annotation sits on the
implementation's method: the interface does not carry it, so the handler
looks the method up on the target's class, **by name and parameter types**.

`SecuredProxy.RequiresPermission` (given) has `String value()`. Write
`SecuredProxy.secure(T target, Class<T> iface, Set<String> permissions)`. It
returns a proxy of `iface` that forwards each call to `target`, except:

- when the target's method carries `@RequiresPermission(p)` and `permissions`
  does not hold `p`, the call throws
  `SecurityException("Missing permission: " + p)`, and **a refused call never
  reaches the target**;
- permission names are compared **by value**.

A method without `@RequiresPermission` is open to everyone, and an exception
the target throws reaches the caller unchanged.

| `permissions` | call | result |
|---|---|---|
| `{READ}` | `getUser(1)` | `"User-1"` |
| `{READ}` | `deleteUser(1)` | `SecurityException: Missing permission: ADMIN`; nothing deleted |
| `{ADMIN}` | `deleteUser(1)` | user 1 deleted |
| `{READ}` | `archive(7)` / `archive("old")` | archived / `SecurityException: Missing permission: ADMIN` |
