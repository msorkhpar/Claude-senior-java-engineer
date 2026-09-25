The default value of a reference is `null`, and following a `null` reference
throws a `NullPointerException`. The page's advice is to check for `null` or to
use `Optional`.

`Directory.Person` has an `address`, and `Directory.Address` has a `city`. Any of
the three can be missing: the person itself, their address, or the address's city.

Write `Directory.cityOf(Person person)`. It returns the person's city, or
`"Unknown"` when any link on the way is `null`.

| person | answer |
|---|---|
| `new Person("Ann", new Address("Oslo"))` | `"Oslo"` |
| `null` | `"Unknown"` |
| `new Person("Bo", null)` | `"Unknown"` |
| `new Person("Cy", new Address(null))` | `"Unknown"` |
