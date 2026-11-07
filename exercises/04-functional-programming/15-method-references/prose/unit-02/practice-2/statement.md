An office building's parcel robot is moving into a new tower that has two levels of underground
parking, floors `-1` and `-2`, with a mail room on each. Before every run, `ParcelRun` groups the
parcels by floor, hands the robot's display the parcel ids, and prints one delivery slip per
parcel. When a recipient takes a parcel, the robot appends their signature to that parcel's slip.

`byFloor` already does the grouping. Finish the three `TODO`s in
`src/main/java/practice/ParcelRun.java`:

- `newFloor()`: the function that makes the list for a floor that has no parcel yet;
- `displayIds(...)`: the ids, in input order, as a `String[]` (the display's API takes nothing else);
- `slips(...)`: one slip per parcel, each starting as `deliver ` followed by the parcel id.

The run must work for every floor of the new tower, and a signature belongs on one slip only.
About four lines in total.
