A record's **compact constructor** runs before the component fields are
assigned, so it is the one place to validate a record's input: every `new`
of the record goes through it.

Complete the compact constructor of

```java
public record Person(String name, int age)
```

so that a `Person` can only be created with valid data:

- a `name` that is `null` or empty (`""`) is refused with an
  `IllegalArgumentException` whose message is `Name cannot be null or empty`;
- a negative `age` is refused with an `IllegalArgumentException` whose message
  is `Age cannot be negative`;
- everything else is accepted unchanged. Leave the generated accessors,
  `equals`, `hashCode` and `toString` alone.

## Examples

```
new Person("Alice", 30)          -> name() = "Alice", age() = 30, prints Person[name=Alice, age=30]
new Person(null, 20)             -> IllegalArgumentException: Name cannot be null or empty
new Person("", 20)               -> IllegalArgumentException: Name cannot be null or empty
new Person("Eve", -1)            -> IllegalArgumentException: Age cannot be negative
new Person("Baby", 0)            -> a valid person
```
