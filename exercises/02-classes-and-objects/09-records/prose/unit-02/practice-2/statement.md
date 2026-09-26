A record may customise what it generates: validate in a compact constructor,
override `toString`, and add methods of its own.

Complete `record Employee(String name, int id, LocalDate hireDate)`:

- **compact constructor**: a `null` name is refused with a
  `NullPointerException` whose message is `Name cannot be null`; a `null`
  hire date with a `NullPointerException` whose message is
  `Hire date cannot be null`; an `id` of 0 or less with an
  `IllegalArgumentException` whose message is `ID must be positive`
  (`Objects.requireNonNull(value, message)` helps);
- **`toString()`**: `Employee(name=<name>, id=<id>, hired on <hireDate>)`;
- **`boolean isNewHire(LocalDate today)`**: `true` when the hire date is
  strictly after the date six months before `today`.

## Examples

```
new Employee("Alice Smith", 4, LocalDate.of(2023, 4, 1)).toString()
        -> "Employee(name=Alice Smith, id=4, hired on 2023-04-01)"
new Employee(null, 1, date)       -> NullPointerException: Name cannot be null
new Employee("Test", 0, date)     -> IllegalArgumentException: ID must be positive
new Employee("Test", 1, null)     -> NullPointerException: Hire date cannot be null

today = 2023-08-22
hired 2023-05-01 -> isNewHire(today) = true
hired 2022-01-01 -> isNewHire(today) = false
hired 2023-02-22 -> isNewHire(today) = false   (exactly six months)
```
