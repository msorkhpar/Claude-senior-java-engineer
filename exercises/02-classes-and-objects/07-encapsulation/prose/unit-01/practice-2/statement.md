The page orders the access levels from most to least restrictive:
**private > default (package-private) > protected > public**.

Write three methods of the enum `AccessLevel`:

- `static AccessLevel of(int modifiers)` reads the level out of the bit set that
  `java.lang.reflect.Member.getModifiers()` or `Class.getModifiers()` returns. Use
  the helpers in `java.lang.reflect.Modifier`.
- `boolean isMoreRestrictiveThan(AccessLevel other)` answers whether this level
  allows access to fewer places than `other`.
- `static AccessLevel mostRestrictive(AccessLevel first, AccessLevel... rest)`
  returns the most restrictive of the levels given.

The constants are declared in alphabetical order, which is **not** the order of
restrictiveness.

Examples:

```
of(Modifier.PUBLIC)                                 -> PUBLIC
of(Modifier.STATIC | Modifier.FINAL)                -> PACKAGE
PACKAGE.isMoreRestrictiveThan(PUBLIC)               -> true
mostRestrictive(PUBLIC, PROTECTED)                  -> PROTECTED
```
