**Constructor chaining**: write the most specific constructor first, and have the
others call it with `this(...)`, so the defaults and the checks live in one place.

Write `Employee` with three constructors:

- `Employee(String name, int id, String department)`;
- `Employee(String name, int id)`: the department is `General`;
- `Employee(String name)`: the id is `0` and the department is `General`.

and the getters `getName()`, `getId()`, `getDepartment()`.

Every constructor refuses a name that is `null` or blank (only whitespace) with an
`IllegalArgumentException`.

**Examples**

```
new Employee("Ann", 7, "Sales")  -> Ann, 7, Sales
new Employee("Ann", 7)           -> Ann, 7, General
new Employee("Ann")              -> Ann, 0, General
new Employee("  ")               -> IllegalArgumentException
```
