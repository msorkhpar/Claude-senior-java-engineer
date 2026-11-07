The depot's intake scanner checks every parcel's weight and longest side before it goes on the
belt. Pallets take much bigger parcels, and since freight was added the scanner still refuses a
12 kg pallet parcel, and the intake screen shows belt limits for it.

`Depot.Parcel` already checks belt parcels correctly. Finish the `TODO` in
`src/main/java/practice/Depot.java`: the constructor of `Depot.FreightParcel`, a parcel that goes
on a pallet (up to 30,000 g, up to 150 cm on the longest side).

When you're done, everything the depot asks a freight parcel, whether it accepts a size and what
its limits are, answers for a pallet, wherever the parcel is handled as a plain `Parcel`. Belt
parcels keep their own limits. About two lines.
