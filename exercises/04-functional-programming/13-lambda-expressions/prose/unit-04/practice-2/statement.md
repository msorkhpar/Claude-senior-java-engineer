A rule used in more than one place belongs in a named constant, and named predicates combine
into bigger ones: `IS_ADULT.and(IS_ACTIVE).and(HAS_VERIFIED_EMAIL)` reads as the rule it is.

The file declares `record User(String name, int age, boolean active, String email, boolean emailVerified)`.
Fill in the constants of `UserFilters` and write one method:

| member | holds for a user when |
|---|---|
| `Predicate<User> IS_ADULT` | the age is 18 or more |
| `Predicate<User> IS_ACTIVE` | the user is active |
| `Predicate<User> HAS_VERIFIED_EMAIL` | the email is not `null` (any non-null string counts, even a blank one) and it is verified |
| `Predicate<User> IS_ELIGIBLE` | all three of the above hold |
| `List<String> eligibleNames(List<User> users)` | returns the names of the eligible users, in order |

| user | eligible? |
|---|---|
| `User("Alice", 30, true, "alice@example.com", true)` | yes |
| `User("Bob", 16, true, null, false)` | no |
| `User("Carol", 40, false, "carol@example.com", false)` | no |

Building `IS_ELIGIBLE` from the other three with `and` keeps the rule in one place; the tests check what it
accepts. Check the exact age at the boundary, an email that is missing, and a user who passes only some
of the rules.
