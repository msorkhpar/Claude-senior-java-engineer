The page's first pitfall is an email check built as a rule framework:

```java
// VIOLATION: Over-engineered email validation
interface ValidationRule { boolean validate(String input); }
class NotNullRule implements ValidationRule { ... }
class NotEmptyRule implements ValidationRule { ... }
class ContainsAtRule implements ValidationRule { ... }
// ... 5 more classes for a simple email check
```

The KISS fix is one method. Write `isValidEmail(String email)` in `Emails`. It looks only at
the **first** `@` of the address, as the page's fix does with `indexOf('@')`, and returns
`true` exactly when that `@` has at least one character before it and at least one after
it. What those characters are does not matter: spaces or another `@` are allowed.

- `isValidEmail("user@example.com")` is `true`;
- `isValidEmail("userexample.com")` is `false`: there is no `@`;
- `isValidEmail("@example.com")` is `false`: nothing comes before the `@`;
- `isValidEmail("user@")` is `false`: nothing comes after it;
- `isValidEmail("a@b@c")`, `isValidEmail("a@@")` and `isValidEmail("a b@c d")` are `true`:
  the first `@` has a character on each side;
- `isValidEmail("@a@")` is `false`: nothing comes before its first `@`;
- `isValidEmail(null)` and `isValidEmail("")` are `false`, and never throw.
