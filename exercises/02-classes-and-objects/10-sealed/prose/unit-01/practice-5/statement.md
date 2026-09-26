The page contrasts enums with sealed types: an enum is a fixed set of
constants, each a single instance that can carry fixed fields, while the kinds
of a sealed type can each carry **different data**, with many instances per
kind. `Cafe` uses both:

- `enum Size { SMALL, MEDIUM, LARGE }` — give each constant its own base price
  in cents through a constructor and a `baseCents()` accessor: SMALL 300,
  MEDIUM 350, LARGE 425;
- `sealed interface Order permits Drink, Pastry, GiftCard`, with records
  `Drink(Size size, int shots)`, `Pastry(String name, int count)` and
  `GiftCard(long cents)`.

Write `static long priceCents(Order order)` with a `switch` over the order:

- a drink costs its size's base price; its first shot (if any) is included, and every
  further shot costs 50;
- pastries cost 250 each;
- a gift card costs its own amount.

**Examples**

- `priceCents(new Drink(Size.SMALL, 1))` -> `300`
- `priceCents(new Drink(Size.MEDIUM, 3))` -> `450`
- `priceCents(new Pastry("croissant", 2))` -> `500`
- `priceCents(new GiftCard(2000))` -> `2000`
