The page's first custom exception is an unchecked `InvalidUserException` that
carries the id of the user whose data was wrong. Write it, and use it.

1. Complete `InvalidUserException` (it extends `RuntimeException`): the
   constructor `InvalidUserException(String message, String userId)` and the
   getter `getUserId()`. The id is a `final` field.
2. Write `UserValidator.validateUsername(String userId, String username)`. It
   returns normally for a usable username and otherwise throws
   `InvalidUserException` with the message `Username cannot be null or empty`
   and the given `userId`.

| call | result |
|---|---|
| `validateUsername("u1", "alice")` | returns normally |
| `validateUsername("u7", "")` | throws `InvalidUserException`, message `Username cannot be null or empty`, `getUserId()` is `"u7"` |

A handler may read the message through `getMessage()` or print the exception,
so decide where the message has to live. A username is unusable when it is
`null`, empty, or only whitespace, where whitespace means what
`String.isBlank()` means (Unicode whitespace, not only the ASCII characters
`trim()` removes).
