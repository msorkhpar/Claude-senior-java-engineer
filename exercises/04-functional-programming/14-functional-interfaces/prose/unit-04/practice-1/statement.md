Validation reads best as small, named `Predicate`s joined with `and()`. Each rule can be tested alone, and because
`and()` short-circuits like `&&`, a rule placed after a null check never sees `null`.

Write two methods in `Validators`, each returning a composed `Predicate<String>`:

1. `email()` accepts text that is not null or empty, contains `@` with at least one character before it, and has a
   `.` somewhere after the `@` with at least one character between the two.
2. `strongPassword()` accepts text of at least 8 characters with an upper-case letter, a lower-case letter, a digit
   and a character that is neither a letter nor a digit.

| input | `email()` | `strongPassword()` |
|---|---|---|
| `"user@example.com"` | `true` | `false` |
| `"not-an-email"` | `false` | `false` |
| `"Passw0rd!"` | `false` | `true` |
| `"password"` | `false` | `false` |

Either predicate may be handed `null`. Check the page's own examples against each rule's exact wording.
