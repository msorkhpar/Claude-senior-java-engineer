Calling a method on `null` throws `NullPointerException`, so a reference that may be
`null` is checked first. An early `return` for the bad input keeps the rest of the method
flat instead of nesting it inside an `if`.

Write `type(String day)` in `Days`. It returns:

- `"Invalid input"` when `day` is `null`;
- `"Weekend"` for Saturday and Sunday, and `"Weekday"` for Monday to Friday, in any mix
  of upper and lower case;
- `"Invalid day"` for anything else.

Examples: `type("Saturday")` is `"Weekend"`, `type("friday")` is `"Weekday"`,
`type("MONDAY")` is `"Weekday"`, `type("Someday")` is `"Invalid day"`, and
`type(null)` is `"Invalid input"`.
