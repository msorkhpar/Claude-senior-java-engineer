`Proxy.newProxyInstance(loader, interfaces, handler)` creates an object that
implements the interfaces and sends **every** call to
`handler.invoke(proxy, method, args)`. Three traps:

- For a method with no parameters, **`args` is `null`**, not an empty array.
- `method.invoke(target, args)` wraps the target's exception in
  `InvocationTargetException`. Rethrowing that checked exception from the
  handler makes the proxy throw `UndeclaredThrowableException`: **rethrow the
  cause**.
- **`equals`, `hashCode` and `toString` reach the handler too**. Forwarding
  `equals` to the target makes `proxy.equals(proxy)` false.

Write `Tracing.trace(iface, target, log)`. It returns a proxy of `iface` that
calls `target` and adds to `log`:

- before the call: `name(arg1, arg2)` (arguments by `String.valueOf`, joined
  by `", "`; `name()` for none);
- after it: `name -> result`, or `name !! ExceptionSimpleName` when the
  target throws, and then the caller gets **the target's own exception**.
- `equals`, `hashCode` and `toString` are answered by the handler and **not
  logged**: `equals` is true only for the proxy itself, `hashCode` is
  `System.identityHashCode(proxy)`, `toString` is `"Traced(" + target + ")"`.

| call on the proxy | returns | log gains |
|---|---|---|
| `greet("World")` | `"Hello, World"` | `greet(World)`, `greet -> Hello, World` |
| `size()` | `3` | `size()`, `size -> 3` |
| `fail("bad")` | throws `IllegalArgumentException("bad")` | `fail(bad)`, `fail !! IllegalArgumentException` |
| `equals(proxy)` | `true` | nothing |
