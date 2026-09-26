A method reference such as `String::toLowerCase` calls its method on whatever element
arrives, so a `null` element throws a `NullPointerException`. The page's fix is to
filter with `Objects::nonNull` before the reference that would dereference it.

`User` is a record with a `name` and an `email`. Write
`Emails.normalised(List<User> users)`: it returns every user's email, lower-cased,
without duplicates, in ascending order.

| users | result |
|---|---|
| `Bob <bob@example.org>`, `Alice <Alice@Example.com>` | `["alice@example.com", "bob@example.org"]` |
| `Alice <alice@example.com>`, `null`, `Bob <no email>`, `Charlie <CHARLIE@example.com>` | `["alice@example.com", "charlie@example.com"]` |

Try to write the whole pipeline in method references. A list can hold `null` users,
a user can have a `null` email, and the same address can be written in different cases.
