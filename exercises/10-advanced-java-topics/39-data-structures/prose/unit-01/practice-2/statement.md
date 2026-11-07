The API gateway keeps each request's headers in a `HashMap<HeaderName, String>`. Last night a
client sent requests with a few thousand made-up header names, all with the same `hashCode()`,
and every gateway node spent its CPU in `HeaderName.equals`. The debug log, which lists a
request's header names from a `TreeSet<HeaderName>`, has never worked either: it fails with a
`ClassCastException`.

`HeaderName` must keep its `equals` and `hashCode`: names are the same header only when they are
spelled exactly alike, and other services rely on the hash. Finish the `TODO` in
`src/main/java/practice/HeaderName.java` (the method and whatever the class needs for it to be
used).

When you're done, the debug log's set works, a lookup among thousands of names that share
one hash code costs a few dozen `equals` calls rather than thousands, and names that differ only
in case stay two headers in the sorted log. About two lines in total. The tests count calls
through `HeaderName.EQUALS_CALLS`.
