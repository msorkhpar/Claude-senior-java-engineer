The page builds a validation framework from an enum: each constant is one rule,
built with a message and a `Predicate<Object>`. A rule's `validate` returns an
empty `Optional` when the value is valid and the message otherwise, and
`validateAll` composes rules. The page's key design point: report **all**
violations in one pass, not just the first.

The starter builds each rule with its message and a placeholder predicate.
Replace each placeholder, and write `validate` and `validateAll`:

- `NOT_NULL`: the value is not `null`.
- `NOT_BLANK`: the value is a `String` that is not blank (not empty and not
  only whitespace).
- `POSITIVE_NUMBER`: the value is a `Number` greater than zero.
- `VALID_EMAIL`: the value is a `String` matching
  `^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$`.
- `Optional<String> validate(Object value)`: empty when valid, else the
  rule's message.
- `static List<String> validateAll(Object value, ValidationRule... rules)`: the
  messages of every rule `value` violates, in the order the rules are given.

Every rule must cope with `null`: it is simply a violation for all of them.

| call | answer |
|---|---|
| `VALID_EMAIL.validate("team@example.org")` | `Optional.empty` |
| `POSITIVE_NUMBER.validate(-1)` | `Optional[Must be positive]` |
| `validateAll(null, NOT_NULL, NOT_BLANK)` | `[Must not be null, Must not be blank]` |
| `validateAll("bad-email", NOT_NULL, NOT_BLANK, VALID_EMAIL)` | `[Must be a valid email]` |
| `NOT_BLANK.validate("   ")` | `Optional[Must not be blank]` |
