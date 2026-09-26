When a filter lambda grows several conditions long, the page suggests giving it
a name: a `Predicate<User> isEligible` reads better in the stream than a block
of `&&` checks, and it can be tested on its own.

`User` is given, nested in `Eligibility`: `record User(String name, int age,
boolean active, boolean verifiedEmail)`. Write two static methods:

1. `isEligible()` returns a `Predicate<User>` that is true for a user who is
   active, at least 18 years old (there is no upper age limit), and has a
   verified email.
2. `eligibleNames(List<User> users)` returns the name of every eligible user in
   alphabetical order: `String`'s natural order (`compareTo`), capitals before
   lower-case letters. A name shared by two eligible users appears twice. Reusing the predicate there keeps the rule in one place.

| user | eligible |
|---|---|
| `User("Alice", 25, true, true)` | yes |
| `User("Bob", 17, true, true)` | no, not an adult |
| `User("Charlie", 30, false, true)` | no, not active |

Mind the exact age limit, and every one of the three conditions.
