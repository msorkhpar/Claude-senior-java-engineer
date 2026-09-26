Two of the page's pitfalls meet at the login form. **A failed login gets one
generic message**, `"Invalid username or password"`, whether the user is
unknown or the password is wrong, so an attacker cannot learn which accounts
exist. And guessing is stopped by an **account lockout**: after 5 failures in
a row the account is locked for 15 minutes, and **a locked account refuses
even the right password**. A success resets the count. Usernames are
**normalized to lower case** (`Locale.ROOT`) before they are tracked, so
capitalisation cannot dodge the lock.

`Credentials` (given) has `exists(user)` and `matches(user, password)`. Write
`LoginGuard(credentials, clock)` with `login(user, password)`:

- a locked account throws `SecurityException("Account temporarily locked")`;
- otherwise a matching password returns `"Welcome, " + user` (normalized) and
  resets the count; anything else returns the generic message and counts one
  failure. The 5th failure locks the account until 15 minutes after it; at
  that instant the lock is over and counting starts again.

| time | call | result |
|---|---|---|
| 09:00 | `login("alice", "s3cret")` | `"Welcome, alice"` |
| 09:00 | `login("ghost", "x")` or `login("alice", "wrong")` | `"Invalid username or password"` |
| 09:01 | 5th wrong password in a row | `"Invalid username or password"`; locked until 09:16 |
| 09:10 | `login("alice", "s3cret")` | `SecurityException` |
| 09:16 | `login("alice", "s3cret")` | `"Welcome, alice"` |
