The page's own example seals an `Animal` class to three kinds: `Dog`, `Cat`
and `Bird`. Dogs and cats end the hierarchy, while birds stay open for further
kinds of bird.

`Zoo` holds that hierarchy as nested classes, still **unsealed**, plus a
`Parrot` that extends `Bird`. Finish it:

1. Make `Animal` **sealed**, permitting exactly `Dog`, `Cat` and `Bird`.
2. Give each permitted subclass the modifier its role needs: `Dog` and `Cat`
   may never be extended, and `Bird` must still allow `Parrot` (and any other
   bird) to extend it.
3. Write `static String sound(Animal animal)` with a `switch` over the
   animal's type:

| animal | sound |
|---|---|
| `Dog` | `Woof` |
| `Cat` | `Meow` |
| `Parrot` | `Squawk` |
| any other `Bird` | `Tweet` |

Once `Animal` is sealed, the switch can cover every kind without a `default`.

**Examples**

- `sound(new Dog())` -> `"Woof"`
- `sound(new Bird())` -> `"Tweet"`
- `sound(new Parrot())` -> `"Squawk"`
