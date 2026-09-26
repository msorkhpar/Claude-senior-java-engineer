The page's Q2: a password kept in a `String` stays in memory until the garbage
collector reclaims it, and it may be pooled. A `char[]` can be **overwritten
as soon as it has been used**, in a `finally` block so that it is cleared on
**every** path. Its pitfall 4 adds: a login is logged by user, never with
the password.

`Authenticator` (given) has `boolean authenticate(char[] password)`. Write
`PasswordLogin.login(user, password, authenticator, log)`:

- append `"Login attempt for user " + user` to `log`, and nothing else;
- return what `authenticator` answers for `password`;
- afterwards every element of the caller's `password` array is `'\0'`,
  whether the login succeeded, failed, or `authenticate` threw (the exception
  reaches the caller).

| authenticator | `login("alice", {s,e,c,r,e,t}, ...)` | `password` afterwards | `log` |
|---|---|---|---|
| accepts `secret` | `true` | `{\0,\0,\0,\0,\0,\0}` | `[Login attempt for user alice]` |
| refuses it | `false` | all `\0` | same |
| throws `IllegalStateException` | throws it | all `\0` | same |
