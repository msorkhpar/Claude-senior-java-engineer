`substring(begin, end)` starts at `begin` and stops before `end`, so its length is
`end - begin`. `substring(begin)` runs to the end of the String.

Write two methods in `Emails`, using `indexOf('@')` to find the separator:

1. `user(String email)` returns the part before the `@`: `user("ada@example.com")` is
   `"ada"`. With no `@`, the whole text is the user: `user("ada")` is `"ada"`.
2. `domain(String email)` returns the part after the `@`: `domain("ada@example.com")` is
   `"example.com"`. With no `@` there is no domain: `domain("ada")` is `""`.
