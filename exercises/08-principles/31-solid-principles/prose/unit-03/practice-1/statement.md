LSP says an object of a subtype must be usable wherever its base type is, without breaking
the program. The page's classic violation is `Square extends Rectangle`:

```java
// Client code breaks with Square
void resize(Rectangle r) {
    r.setWidth(5);
    r.setHeight(3);
    assert r.area() == 15; // FAILS with Square! area = 9
}
```

`Rectangle`'s contract is that `setWidth(w)` changes only the width and `setHeight(h)`
only the height. `Contracts` already holds `Rectangle` and the page's `Square`.

Write `honoursRectangleContract(Rectangle r)`, a **contract test**: it runs the page's
client code on `r`, in the page's order (`setWidth(5)`, then `setHeight(3)`), and returns
`true` only when, after that, `getWidth()` is `5`, `getHeight()` is `3` and `area()` is
`15`. It reads the object only through those public methods, as any client would: a
subclass may keep its sides somewhere else.

- `new Rectangle(1, 1)` honours it, and `new Square(4)` does not.
- It judges **behaviour, not type**: a subclass written later that breaks the contract is
  caught, and one that keeps it is accepted. Checking `instanceof Square`, or the exact
  class, is the smell the page warns about, not a test.
- A subtype can get any one of the three answers wrong while the other two look right, so
  all three are checked.
