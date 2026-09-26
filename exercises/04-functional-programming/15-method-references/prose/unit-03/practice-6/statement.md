Predicates made from method references combine: `p.and(q)`, `p.or(q)`,
`Predicate.not(p)`. The page builds its eligibility rule from two static helpers
(`Eligibility::isAdult`, `Eligibility::hasEmail`) and a record accessor used as an
unbound reference (`User::verified`), and collects to a typed array with
`toArray(String[]::new)`.

`User` is a record `(String username, String email, boolean verified, int age)`. Complete
`Eligibility`:

- `static boolean isAdult(User u)`: the user is 18 or older.
- `static boolean hasEmail(User u)`: the email is present and not blank.
- `List<String> eligible(List<User> users)`: usernames of users who are adult **and**
  verified **and** have an email, in order.
- `String[] unverified(List<User> users)`: usernames of the users who are not verified,
  in order.

| user | eligible? | unverified? |
|---|---|---|
| alice, `alice@example.com`, verified, 25 | yes | no |
| bob, `""`, not verified, 30 | no | yes |
| charlie, `c@example.com`, not verified, 16 | no | yes |
| diana, `d@example.com`, verified, 22 | yes | no |

Mind the boundary age, and the ways an email can be missing.
