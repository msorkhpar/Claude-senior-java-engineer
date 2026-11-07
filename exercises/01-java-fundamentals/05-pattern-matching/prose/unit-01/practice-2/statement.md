The gateway reads its CORS origins with `settings.strings("allowed-origins")`. The setting comes
from three places: a YAML file (a `List`), the command line (a `String[]`), or a plugin written
against an older API (an `Object[]`). Last week someone wrote a port number into the YAML list.
Startup went fine, and the first browser request failed with a `ClassCastException` deep in the
CORS filter.

Finish `stringList(Object value)` in `src/main/java/practice/Settings.java`. It returns the
strings, in order, from a list or an array whose items are all strings, and throws
`IllegalArgumentException` for any other value, so a bad setting stops the gateway at startup
and never reaches the filter. About fifteen lines.
