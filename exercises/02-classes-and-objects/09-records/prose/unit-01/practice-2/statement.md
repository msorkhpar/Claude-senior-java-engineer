A compact constructor may not only validate its parameters, it may also
**modify** them: assigning to a parameter inside the compact constructor
changes the value the component field receives. Everything the record
generates, `equals` and `hashCode` included, then works on the stored value.

Complete

```java
public record Tag(String value)
```

so that tags are stored in one canonical form:

- the stored `value` has no leading or trailing whitespace and is lower case
  (`String.strip()` and `toLowerCase(Locale.ROOT)`);
- a `null` value, or one that is empty after stripping, is refused with an
  `IllegalArgumentException`;
- because the value is normalised when it is stored, two tags that differ only
  in case or surrounding spaces are `equal` and have the same `hashCode`, so a
  `Set<Tag>` keeps only one of them.

## Examples

```
new Tag("  Java ").value()                    -> "java"
new Tag("RECORDS").equals(new Tag("records")) -> true
a HashSet given new Tag("Java") and new Tag(" java") -> holds one tag
new Tag("   ")                                -> IllegalArgumentException
new Tag(null)                                 -> IllegalArgumentException
```
