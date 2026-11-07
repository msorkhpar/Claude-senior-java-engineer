The document service checks every request against the user's grants: the base grants of their
role, plus extra grants an owner gave them, which are stored in the `extra_grants` column as the
grants' names separated by commas (`COMMENT,SHARE`). Support has two tickets open. Since Monday's
release, every editor can share documents, although only one editor was ever given that right.
And the members page fails to open for any user with no extra grants at all.

The role table in `src/main/java/practice/Grants.java` is right. Finish its three `TODO`s:

- `base(...)`: a role's base grants, as handed to the rest of the service;
- `effective(...)`: what one user may do;
- `decode(...)`: reading a stored `extra_grants` value.

When you're done, nothing a caller does with the sets it receives, and no user's grants, change
what a role grants, and every value in the column reads back. About ten lines in total.
