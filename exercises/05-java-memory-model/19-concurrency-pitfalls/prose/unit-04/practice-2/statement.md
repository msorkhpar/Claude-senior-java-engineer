The page's `UnsafeRegistry` does `if (!containsKey(name)) put(name, endpoint)`
on a `HashMap`: a check-then-act race on a map that is not thread-safe. Its
fix uses `ConcurrentHashMap` and one **atomic** call instead of two, and the
page's best practices prefer `computeIfAbsent` to a manual check-then-act,
because its function runs **at most once per key**.

Write `ServiceRegistry`, safe to use from many threads:

- `register(name, endpoint)` adds the service and returns `true`, or returns
  `false` and changes nothing when the name is already registered.
- `lookup(name)` returns `Optional.of(endpoint)`, or `Optional.empty()`.
- `unregister(name)` removes it and returns whether it was there.
- `names()` returns the registered names.
- `resolve(name, resolver)` returns the endpoint for `name`, calling
  `resolver.apply(name)` to find it when it is not registered yet, and
  registering the result. Two threads resolving the same unknown name at
  once call the resolver **once**.

| calls | result |
|---|---|
| `register("auth", "http://auth:8080")` | `true` |
| `register("auth", "http://other:1")` | `false`; `lookup("auth")` is still `http://auth:8080` |
| `resolve("billing", n -> "http://" + n + ":9000")` | `http://billing:9000`, now registered |
| `unregister("nope")` | `false` |
| another thread's `resolve("billing", …)` is still resolving; `register("billing", "http://other:1")` | `false`, once that resolve ends; `lookup("billing")` is the resolved endpoint |
