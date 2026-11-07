At the sorting hub, every parcel passes two scanners, and a jam sends some parcels past the same
scanner again a few minutes later. The loading list is supposed to hold each parcel once, but the
drivers' lists have started showing the same parcel two or three times, and the loaders put it on
the truck that many times in their count.

`HubScans.loadingList(...)` already reads the scanner lines into `Parcel`s. Finish the three
`TODO`s in `src/main/java/practice/HubScans.java`:

- `Parcel.equals(...)` and `Parcel.hashCode()`: a parcel is identified by its carrier and its
  tracking number; the scan time is not part of what it is;
- `unique(...)`: each parcel once, as it was first scanned, in the order of the scans.

Keep `Parcel` a class. About six lines in total.
