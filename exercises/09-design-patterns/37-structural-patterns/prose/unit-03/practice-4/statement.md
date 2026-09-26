The page's Q3: `Proxy.newProxyInstance` builds, at run time, an object that
implements the interfaces you name and sends every call to one
`InvocationHandler`. One handler can then add logging to any interface.

Given: the interface `Store` (`get`, `put`, `size`). Write
`Tracing.logging(target, type, log)`, returning a proxy of `type` that
delegates each call to `target` and appends one entry per call to `log`:

- the entry is the method name and its arguments, joined by `", "`:
  `put(a, 1)`; **a call without arguments is logged with empty parentheses**,
  though the handler gets `null` for them;
- **the target's own exception reaches the caller**: `Method.invoke` wraps it
  in an `InvocationTargetException`, which the page says to unwrap;
- **Object methods go to the target and are not logged**: `toString`,
  `hashCode` and `equals` reach the handler too, and the page warns to handle
  them.

| call on the proxy | returns | log adds |
|---|---|---|
| `put("a", "1")` | what the target returns | `put(a, 1)` |
| `get("a")` | `"1"` | `get(a)` |
| `size()` | `1` | `size()` |
| `get("zzz")`, target throws `NoSuchElementException` | that exception | `get(zzz)` |
| `toString()` | the target's `toString()` | nothing |
