An object's **fields** hold its state and its **methods** are its behaviour. The
methods are what keep the state valid: here, no quantity is ever negative. The page
also warns against **forgetting to initialise fields**: a field must hold a usable
value from the moment the object exists, set at its declaration or in the
constructor.

Write `Inventory`:

- `Inventory()` creates an empty inventory;
- items are identified by their name's **text**: two `String` objects with the same
  characters are the same item;
- `add(String item, int quantity)` adds stock (`quantity` is positive);
- `remove(String item, int quantity)` takes stock away. Removing more than is held
  throws `IllegalArgumentException`, and then nothing changes;
- `quantityOf(String item)` returns the quantity held, `0` for an unknown item;
- `items()` returns the names of the items held, sorted alphabetically. An item whose
  quantity reached `0` is not listed.

**Examples**

```
inv = new Inventory()
inv.quantityOf("bolt")      -> 0
inv.add("bolt", 5); inv.remove("bolt", 2)
inv.quantityOf("bolt")      -> 3
inv.remove("bolt", 4)       -> IllegalArgumentException, still 3
inv.remove("bolt", 3); inv.items() -> []
```
