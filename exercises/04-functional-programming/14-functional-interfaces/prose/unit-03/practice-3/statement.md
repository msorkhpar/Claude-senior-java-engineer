The depot's sorter sends each parcel to a bin by the first two characters of its postcode. Since
the depot started accepting parcels before the recipient's details are complete, the sorter stops
dead now and then with a `NullPointerException`, and every parcel behind the one it choked on
waits. Those parcels should go to the `HOLD` shelf instead, and the rest should keep moving.

`Sorting` already has the three steps from a parcel to its postcode, and `binFor(...)` turns a
postcode into a bin. Finish the `TODO` in `src/main/java/practice/Sorting.java`: `postcodeOf(...)`
returns the parcel's postcode, or nothing when the depot can't route the parcel yet, whichever of
its parts is missing. About four lines.
