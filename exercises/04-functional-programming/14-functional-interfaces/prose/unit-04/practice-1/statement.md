Validation reads best as small, named `Predicate`s joined with `and()`. Each rule can be tested alone, and because
`and()` short-circuits like `&&`, a rule placed after a null check never sees `null`.

Write two methods in `Validators`, each returning a composed `Predicate<String>`:

1. `email()` accepts text that is not null or empty and contains `@`, where, taking the **first** `@` and the **last**
   `.`, at least one character comes before the `@` and at least one character lies between the `@` and that `.`.
2. `strongPassword()` accepts text of at least 8 characters (8 is enough) with an upper-case letter, a lower-case
   letter, a digit and a character that is neither a letter nor a digit. Letter case and digits are as
   `Character.isUpperCase`, `isLowerCase`, `isDigit` and `isLetterOrDigit` define them, so `Ä` is an upper-case letter.

| input | `email()` | `strongPassword()` |
|---|---|---|
| `"user@example.com"` | `true` | `false` |
| `"not-an-email"` | `false` | `false` |
| `"Passw0rd!"` | `false` | `true` |
| `"password"` | `false` | `false` |

Either predicate may be handed `null`. Check the page's own examples against each rule's exact wording.
