A subclass declared with `extends` inherits the superclass's non-private fields
and methods, and adds its own. Constructors are **not** inherited: a subclass
constructor hands its part of the state up with `super(...)`. An overriding
method (marked `@Override`) runs in place of the superclass version, and
`super.method()` still reaches the superclass version.

`Animals` holds two nested classes, taken from the lesson's own example.
Write their bodies:

- `Animal(String name)` stores the name in the `protected` field `name`;
  `getName()` returns it; `makeSound()` returns `"The animal makes a sound"`.
- `Dog(String name)` is an `Animal`. Its constructor gives the name to
  `Animal`: `Dog` declares **no** field of its own.
- `Dog.makeSound()` overrides the sound: `"<name> barks: Woof! Woof!"`.
- `Dog.makeAnimalSound()` returns the sound **`Animal`'s version** makes.
- `Dog.wagTail()` returns `"<name> is wagging its tail"`.

## Examples

```
Animal pet = new Dog("Rex");
pet.makeSound()               -> "Rex barks: Woof! Woof!"
((Dog) pet).wagTail()         -> "Rex is wagging its tail"
new Dog("Rex").makeAnimalSound() -> "The animal makes a sound"
new Dog("Rex").getName()      -> "Rex"
new Animal("Generic").makeSound() -> "The animal makes a sound"
```
