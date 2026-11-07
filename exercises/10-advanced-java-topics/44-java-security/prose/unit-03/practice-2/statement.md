The support portal is moving its users off a table of password digests that leaked in a
penetration test: the testers matched most rows in an afternoon. Each user now gets one `Stored`
row (salt, iteration count, hash). The login service, run by another team, verifies against the
same rows with `PBKDF2WithHmacSHA256` and a 256-bit output, using the salt and count it finds in
the row. Operations raise the iteration count every year or so; nobody wants to force a password
reset when they do.

Finish `hash`, `verify` and `derive` in `src/main/java/practice/PasswordHasher.java`. When you're
done:

- `verify` accepts the password a row was made from and refuses any other;
- two users who choose the same password get rows that don't show it;
- a row written under an older iteration count still verifies after the count is raised;
- the login service's own PBKDF2 computation over a row's salt and count gives the row's hash.

The tests use small iteration counts so that they run quickly. About ten lines in total.
