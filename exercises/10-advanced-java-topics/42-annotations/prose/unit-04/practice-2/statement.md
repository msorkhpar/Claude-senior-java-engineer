Spring's step 6 (Q1) calls `@PostConstruct` methods after creating a bean
and `@PreDestroy` methods before discarding it. The page's version reads both
annotations through reflection and runs the methods sorted by their `order()`.

`@PostConstruct(order)` and `@PreDestroy(order)` are given. Write:

- `initialize(Object target)`: call every no-argument method `target`'s class
  declares with `@PostConstruct`, lowest `order` first.
- `destroy(Object target)`: the same for `@PreDestroy`.

Callbacks may be `private`, and **private callbacks run too**. When a callback
throws, `Method.invoke` wraps the exception in `InvocationTargetException`;
**rethrow the callback's own exception** instead.

| class | call | log |
|---|---|---|
| `@PostConstruct(order = 2) authenticate()`, `@PostConstruct(order = 3) bindPorts()`, `@PostConstruct(order = 1) connect()` | `initialize` | `[connect, authenticate, bindPorts]` |
| seven callbacks with orders 0 to 6, declared in another order | `initialize` | lowest `order` first |
| `@PreDestroy(order = 2) closeConnections()`, `@PreDestroy(order = 1) flushBuffers()` | `destroy` | `[flushBuffers, closeConnections]` |
| `@PostConstruct private void prepare()` | `initialize` | `[prepare]` |
| `@PostConstruct void load()` throws `IllegalStateException("no config")` | `initialize` | `IllegalStateException: no config` |
