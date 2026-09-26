A record body may hold instance methods, static methods (such as a factory),
implementations of the interfaces the record declares, and overrides of the
methods the record generates.

`Person.java` declares the interface

```java
interface Printable {
    String print();
}
```

and the record `Person(String name, int age) implements Printable`. Write its
four methods:

- `boolean isAdult()`: `true` when the age is 18 or more;
- `static Person createAdult(String name)`: a person of that name, aged 18;
- `String print()`: the line `Person: <name>, <age> years old`;
- `String toString()`: overridden to read `Person named <name> is <age> years old`.

Do not touch `equals` or `hashCode`: the generated ones must keep comparing the
components.

## Examples

```
new Person("Alice", 30).isAdult()     -> true
new Person("Tim", 17).isAdult()       -> false
Person.createAdult("Frank")           -> Person with name "Frank", age 18
new Person("Alice", 30).print()       -> "Person: Alice, 30 years old"
new Person("Bob", 25).toString()      -> "Person named Bob is 25 years old"
```
