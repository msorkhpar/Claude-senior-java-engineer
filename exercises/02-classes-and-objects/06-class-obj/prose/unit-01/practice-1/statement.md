A class bundles **fields** (its state), **constructors** (how that state is first
set), **methods** (its behaviour) and, sometimes, **nested classes**. Keep the
fields private, so every change goes through a method that can check it.

Write the class `Person`:

- `Person(String name, int age)` creates a person.
- `getName()` and `getAge()` return the current values.
- `setAge(int age)` changes the age. A negative age is refused with an
  `IllegalArgumentException` whose message is `Age cannot be negative`, and the
  person keeps the age it had.
- The constructor applies the same rule: a negative age is refused.
- `introduce()` returns `Hello, my name is <name> and I am <age> years old.`

Also write the static nested class `Person.Address`:

- `Address(String street, String city)`;
- `getFullAddress()` returns the street, a comma and a space, then the city.

**Examples**

```
new Person("Ada", 36).introduce()      -> "Hello, my name is Ada and I am 36 years old."
p.setAge(-1)                            -> IllegalArgumentException("Age cannot be negative")
new Person.Address("1 Main St", "Springfield").getFullAddress() -> "1 Main St, Springfield"
```
