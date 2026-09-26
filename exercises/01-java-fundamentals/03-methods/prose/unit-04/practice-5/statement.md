Overloading and overriding are easy to confuse. An overload is another method with the same
name and different parameters, and the compiler picks it. An override replaces a
superclass method with the same name and parameters, and the JVM picks it at run time from
the object's class, even when the variable is declared as the superclass.

`Sounds` holds two classes. Complete them:

1. `Animal.speak()` returns `"..."`.
2. `Animal.speak(int times)`, an overload, returns the animal's sound `times` times,
   separated by single spaces, and `""` for `0`. It must use the animal's own sound.
3. `Dog.speak()` overrides `speak()` and returns `"Woof"`.

Examples: `new Animal().speak(2)` is `"... ..."`. With `Animal a = new Dog();`, `a.speak()`
is `"Woof"`, and `a.speak(3)` is `"Woof Woof Woof"`: `speak(int)` is inherited, and the
`speak()` it calls is the dog's.
