The parcel service receives tracking webhooks from its carriers. Each payload carries a `"type"`
field, and the decoder looks that name up in a map to find the record class to decode into. The
map was written by hand, and when the carriers' `Damaged` events were split into `Crushed` and
`Soaked`, nobody updated it: for two days every damage report was rejected as an unknown type.

The payload types already form sealed hierarchies of records, so the decoder should build the map
from the hierarchy at startup. `of(...)` and `register(...)` are written. Finish the `TODO` in
`src/main/java/practice/PayloadTypes.java`:

- `collect(...)`: add every record class a value of the given type can be.

When you're done, `of(...)` returns every payload record of a hierarchy however it is nested, and
nothing else. A map it can't make complete is worse than none, so for such a type `of(...)`
throws `IllegalArgumentException` instead of returning. About eight lines in total.
