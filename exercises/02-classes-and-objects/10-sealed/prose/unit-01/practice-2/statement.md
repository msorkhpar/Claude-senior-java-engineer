Every permitted subtype of a sealed type must say how the hierarchy goes on,
with exactly one of three modifiers:

- `final`: nothing may extend it;
- `sealed`: only the subtypes it permits may extend it;
- `non-sealed`: anything may extend it again.

A class with no sealed supertype needs none of them and is simply open.

Write `static String kindOf(Class<?> type)` that reads these rules off a
loaded class with reflection and returns one of `"final"`, `"sealed"`,
`"non-sealed"` or `"open"`:

- `"final"` when the type is final (a record is implicitly final);
- `"sealed"` when the type is sealed (`Class.isSealed()`), a class or an interface;
- `"non-sealed"` when it is neither, and one of its **direct** supertypes (its
  superclass or an interface it names directly) is sealed;
- `"open"` otherwise.

**Examples** (with `sealed interface Pet permits Cat, Dog, Fish`)

- `kindOf(Pet.class)` -> `"sealed"`
- `kindOf(Goldfish.class)` where `final class Goldfish extends Fish` -> `"final"`
- `kindOf(Object.class)` -> `"open"`
