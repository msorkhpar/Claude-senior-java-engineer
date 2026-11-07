Users can now pick the time zone the web app shows dates in. `DisplaySettings` lives in the HTTP
session, and in production the cluster replicates every session to a second node by serializing
it, so the settings must come back intact on the other node. Callers ask `timeZone()`, which
returns `Optional<String>`, and fall back to the server's zone when it is empty.

Finish `setTimeZone` and `timeZone` in `src/main/java/practice/DisplaySettings.java`, adding the
field they need. About three lines in total.
