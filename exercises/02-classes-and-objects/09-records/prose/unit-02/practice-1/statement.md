The one line `record Person(String name, int age) {}` gives you a constructor,
accessors, `equals`, `hashCode` and `toString`. A traditional class has to write
them by hand, and every hand-written one is a chance for a mistake.

`PersonBean` is the traditional-class version of that record: its fields,
constructor and accessors are written. Write the other three so that a
`PersonBean` behaves exactly like the record would:

- `equals(Object)`: `true` for another `PersonBean` with an equal `name` (which
  may be `null`) and the same `age`; `false` for `null` and for any other type;
- `hashCode()`: equal beans have equal hash codes (`Objects.hash` helps);
- `toString()`: `PersonBean[name=<name>, age=<age>]`, the record's format.

## Examples

```
new PersonBean("Alice", 30).equals(new PersonBean("Alice", 30))   -> true
new PersonBean("Alice", 30).equals(new PersonBean("Alice", 31))   -> false
new PersonBean(null, 5).equals(new PersonBean(null, 5))           -> true
new PersonBean("Alice", 30).equals("Alice")                       -> false
new PersonBean("Alice", 30).toString()                            -> "PersonBean[name=Alice, age=30]"
```
