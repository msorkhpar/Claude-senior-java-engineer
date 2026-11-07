The security team searches the access audit log by user, by role and by code, and their saved
searches match lines such as `k.mensah [billing,support] W+ /invoices/88`. Requests from visitors
who are not signed in must be findable with a `-` in the user column. The code column is the
operation (`R`, `W` or `D`) followed by `+` when the request was allowed and `-` when it was
refused.

`AuditLine.line` already puts the columns together. Write the three `TODO` methods in
`src/main/java/practice/AuditLine.java`: `who(user)`, `roles(roles)` and `code(op, outcome)`.

About three lines in total.
