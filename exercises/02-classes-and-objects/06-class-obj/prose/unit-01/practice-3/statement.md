An **immutable** object never changes after it is built. The page's recipe: make
the class `final`, make every field `private final`, set them all in the
constructor, and offer getters only. Remember what `final` on a field does and
does not do: it fixes the **reference**, but a mutable object behind it (a `List`,
say) can still change, so such a field needs a copy on the way in and a read-only
view on the way out.

Write `ImmutablePerson`:

- `ImmutablePerson(String name, int age, List<String> nicknames)`;
- `getName()`, `getAge()` and `getNicknames()`;
- `withAge(int age)` returns a **new** `ImmutablePerson` with that age and the same
  name and nicknames. The original does not change.

Rules:

- the class cannot be subclassed;
- every field is `private` and `final`;
- nothing the caller does to the list it passed in reaches the person;
- the list `getNicknames()` returns cannot be modified (any attempt throws).

**Examples**

```
p = new ImmutablePerson("Ada", 36, ["Countess"])
p.withAge(37).getAge()   -> 37
p.getAge()               -> 36
p.getNicknames().add("x") -> throws UnsupportedOperationException
```
