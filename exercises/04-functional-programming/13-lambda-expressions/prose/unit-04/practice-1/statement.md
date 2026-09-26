A `filter` lambda with five `if` statements in it is a puzzle. Moving the rules into a named
method turns the pipeline into a sentence: `filter(ItemRules::isValidItem)`.

The file declares `enum Status { ACTIVE, DELETED }`, `record Owner(String name, boolean verified)`
and `record Item(String id, int value, boolean active, Status status, Owner owner)`. Write two
static methods of `ItemRules`:

1. `boolean isValidItem(Item item)`: the item is not `null`, its value is above zero, it is
   active, its status is not `DELETED`, and it has an owner who is verified.
2. `List<String> validIds(List<Item> items)`: the ids of the valid items, in order. Passing
   `isValidItem` as a method reference reads best, though the tests check only the ids.

| item | valid? |
|---|---|
| `Item("a", 10, true, ACTIVE, Owner("Ann", true))` | yes |
| `Item("b", 10, false, ACTIVE, Owner("Ann", true))` | no: inactive |
| `Item("c", -5, true, ACTIVE, Owner("Ann", true))` | no: value not above zero |
| `Item("d", 10, true, ACTIVE, Owner("Ben", false))` | no: owner not verified |

Every rule on the page's list counts, including the ones about things that may be missing.
