A **copy constructor** builds an object from another object of the same class.
It is how you hand out an independent copy: the copy must not share anything
mutable with the original, or a change to one shows up in the other.

Write `Contact`:

- `Contact(String name, LocalDate dateOfBirth, String email, List<String> phones)`:
  `name` and `dateOfBirth` are required. A `null` one throws `NullPointerException`
  with the message `Name cannot be null` or `Date of birth cannot be null`.
  The contact keeps its **own** list of phones.
- `Contact(String name, LocalDate dateOfBirth)`: chains to the full constructor,
  with no email (`null`) and an empty phone list.
- `Contact(Contact other)`: the copy constructor.
- `getName()`, `getDateOfBirth()`, `getEmail()`, `getPhones()`, and
  `addPhone(String phone)`.

**Examples**

```
ada  = new Contact("Ada", 1990-01-01, "ada@example.com", ["555-0100"])
copy = new Contact(ada)
ada.addPhone("555-0199")
copy.getPhones()                    -> ["555-0100"]
new Contact(null, 1990-01-01)       -> NullPointerException("Name cannot be null")
```
