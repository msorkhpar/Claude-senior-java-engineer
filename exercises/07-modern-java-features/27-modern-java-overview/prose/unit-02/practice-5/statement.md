Java 11's nest-based access control (JEP 181) groups a top-level class and all
the classes nested in it into one **nest**. Nestmates reach each other's
private members directly, with no synthetic accessor methods, and reflection
can see the nest: `Class.getNestHost()` returns the nest host (the
**outermost** enclosing class), and `Class.isNestmateOf(Class)` tells whether
two classes share a nest. The page adds that `isNestmateOf` is true for the
class itself.

Write the class `Nests`:

- `host(Class<?> c)` returns the host of `c`'s nest: the **top-level** class
  that `c` is nested in, however deep, or `c` itself when `c` is top-level. Local
  and anonymous classes (which have no declaring class) belong to the nest of the
  top-level class whose code declares them.
- `nestmates(Class<?> a, Class<?> b)` returns whether `a` and `b` are in the
  same nest: **any** two classes nested in one top-level class, the top-level
  class itself, and a class paired with itself.

With `class Outer { class Inner { class Deeper {} } class Sibling {} }` and an
unrelated top-level `class Stranger {}`:

| call | answer |
|---|---|
| `host(Outer.Inner.class)` | `Outer.class` |
| `host(Outer.Inner.Deeper.class)` | `Outer.class` |
| `host(Outer.class)` | `Outer.class` |
| `nestmates(Outer.class, Outer.Inner.class)` | `true` |
| `nestmates(Outer.Inner.Deeper.class, Outer.Sibling.class)` | `true` |
| `nestmates(Outer.class, Stranger.class)` | `false` |
