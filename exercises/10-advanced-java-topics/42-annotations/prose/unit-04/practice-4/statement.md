The page's best practice: **validate annotations early**, at startup, not
while requests are being served, and give **meaningful error messages**. A
framework that finds a broken `@OnStart` method only when the first bean is
started fails in production; one that checks when it reads the class fails
at boot, with every problem listed.

`CallbackPlan.OnStart` (given) has `int order() default 0`. Write
`CallbackPlan`:

- `CallbackPlan.of(Class<?> type)` reads the `@OnStart` methods that `type`
  declares (any access level). An `@OnStart` method must take no parameters;
  if any takes some, `of` throws `IllegalArgumentException` **at once, before
  any bean exists**, and its message names **every** such method.
- `names()` returns the callbacks' names in run order: lowest `order` first.
- `run(Object bean)` calls the callbacks on `bean` in that order.

| class | `of(...)` | `names()` |
|---|---|---|
| `@OnStart(order = 2) warmUp()`, `@OnStart(order = 1) open()` | a plan | `[open, warmUp]` |
| seven callbacks with orders 0 to 6, declared in another order | a plan | lowest `order` first |
| `@OnStart open(String url)`, `@OnStart load(int n)`, `@OnStart ready()` | `IllegalArgumentException` naming `open` and `load` | |
