A switch expression over an enum that names every constant needs no `default`: the compiler
checks that nothing is left out, and refuses the switch if a constant is added later and
not handled. A `default` arm would hide that missing case instead.

`Palette` gives you `enum Color { RED, ORANGE, YELLOW, GREEN, BLUE, VIOLET }`. Write
`shade(Color color)` as one switch expression with no `default`:

- `RED`, `ORANGE` and `YELLOW` are `"Warm"`;
- `GREEN`, `BLUE` and `VIOLET` are `"Cool"`;
- `null` is `"None"`. Without a `case null` label, a switch throws `NullPointerException`
  on `null`; since Java 21 a `case null` arm handles it.
