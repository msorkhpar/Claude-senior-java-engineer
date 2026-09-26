A subclass **inherits** the non-private members of its superclass, can **override**
a method to give its own version (mark it `@Override`), and can call the
superclass's version with `super.method()`. Constructors are not inherited: the
subclass constructor calls `super(...)`.

Write `Animal` and its nested static subclass `Animal.Dog`:

`Animal(String name)`:

- `getName()` returns the name;
- `makeSound()` returns `The animal makes a sound`;
- `describe()` returns `<name> the animal`.

`Dog(String name, String breed)` extends `Animal`:

- `getBreed()` returns the breed;
- `makeSound()` returns `The dog barks`, and does so however the dog is referred to;
- `fetch()` returns `<name> is fetching`;
- `describe()` returns the animal's description followed by ` (<breed>)`, built on
  the superclass's version.

**Examples**

```
Animal a = new Animal.Dog("Buddy", "Labrador")
a.getName()     -> "Buddy"
a.makeSound()   -> "The dog barks"
a.describe()    -> "Buddy the animal (Labrador)"
new Animal("Generic").makeSound() -> "The animal makes a sound"
```
