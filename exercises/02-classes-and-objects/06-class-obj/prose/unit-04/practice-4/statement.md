The **Liskov Substitution Principle**: an object of a subclass must be usable
anywhere its superclass is expected, without the caller noticing. The page's
example is a `Square` that extends `Rectangle`: setting a square's width also
changes its height, which a caller of `Rectangle` does not expect.

`RectangleContract` already holds the classes `Rectangle` (independent width and
height) and `Square` (its setters keep both sides equal). Write the probe:

`static boolean honoursRectangleContract(Rectangle r)` sets the width to `5`, then
the height to `4`, and returns `true` only if the rectangle then reports width `5`,
height `4` and area `20`. It may change the rectangle it is given.

Judge the object only by what it **does**, never by what class it is: any
subclass that behaves like a rectangle passes, and any that does not fails.

**Examples**

```
honoursRectangleContract(new Rectangle(2, 3))  -> true
honoursRectangleContract(new Square(3))        -> false  (ends as 4 x 4)
```
