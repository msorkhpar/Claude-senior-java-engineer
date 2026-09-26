**Dynamic method dispatch**: a call to an overridden method is resolved at
run time from the object's **actual class**, not from the type of the
reference. Every non-static, non-private method is virtual.

In `Pets`, the lesson's `Animal`, `Dog` and `Cat`, and two more:

- `Animal.sound()` returns `"The animal makes a sound"`.
- `Dog` overrides it: `"The dog barks"`. `Cat` overrides it: `"The cat meows"`.
- `Puppy extends Dog` is given and overrides nothing.
- `Kitten extends Cat` overrides `sound()` to return the cat's sound followed
  by `" softly"`: reuse the `Cat` version, do not repeat its text.
- `Pets.chorus(List<? extends Animal> animals)` returns every animal's sound,
  in order, joined with `" / "`; no animals give `""`.

Let each class answer for itself. Do not test for the class inside `Animal`
(no `instanceof` or `getClass()`): that is exactly the work dispatch does.

## Examples

```
Animal myPet = new Dog();  myPet.sound() -> "The dog barks"
myPet = new Cat();         myPet.sound() -> "The cat meows"
new Puppy().sound()                      -> "The dog barks"
new Kitten().sound()                     -> "The cat meows softly"
chorus([Dog, Cat, Animal]) -> "The dog barks / The cat meows / The animal makes a sound"
chorus([])                 -> ""
```
