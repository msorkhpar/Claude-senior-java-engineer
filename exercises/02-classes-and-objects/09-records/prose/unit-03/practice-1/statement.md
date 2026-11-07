The nightly import reads rows from partner files, each row a map from column name to value, and
turns every row into an object with `RowMapper.map`. The mapper was written for beans. The team
has since turned `Shipment`, `Sensor` and the other imported types into records, and now every
import fails before the first row is stored.

Finish `toRecord(...)` in `src/main/java/practice/RowMapper.java`, so that `map` builds records as
well as beans. Each column is named after a component of the record, but partners send the
columns in whatever order their files have, and the import adds bookkeeping columns of its own
(their names start with `_`). A row that a record refuses must still be refused, with the
record's own exception somewhere in what `map` throws. About ten lines.

The tests use records of their own, with components of several types.
