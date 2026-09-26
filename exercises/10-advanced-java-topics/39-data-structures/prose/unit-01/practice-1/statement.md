Hash collections find a key by `hashCode()` first (which bucket) and
`equals()` second (which entry). The page's pitfall: objects that are equal
but **hash differently land in different buckets**, so a `HashSet` keeps both
and `contains` misses.

Write the final class `Tag`, a label compared **without regard to case**:

- `new Tag(name)` keeps `name` as given (`null` is refused with
  `NullPointerException`); `name()` returns it.
- `equals` is true for another `Tag` whose name differs only in case, and
  false for `null` or any other type.
- `hashCode` **agrees with `equals`**: equal tags have equal hash codes.
- The result must not depend on the JVM's **default locale**.

| tags | result |
|---|---|
| `Tag("Java")` vs `Tag("JAVA")` | equal |
| `Tag("Java")` vs `Tag("Kotlin")` | not equal |
| `HashSet` of `Java`, `JAVA`, `java` | size 1, contains `Tag("jAvA")` |
| `Tag("Java").equals("Java")` | `false` |
