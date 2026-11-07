The sign-up service was moved from Java 8 to Java 21, and its rules class was tidied on the way:
the reserved user names became an unmodifiable set built once at startup. Since the release,
three tickets are open:

- a mobile client that leaves out the username field gets a 500 instead of the form's
  "username is required" message;
- the nightly audit export sometimes lists a domain that was blocked after the export started;
- monitoring raises "status page changed" after restarts in which no limit changed.

Finish the three `TODO`s in `src/main/java/practice/SignupRules.java`: `isReserved(...)`,
`blockedDomains()` and `limitsReport(...)`. Each method's comment says what its caller needs.
About five lines in total.
