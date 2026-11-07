The warehouse service records each parcel's dispatch moment from an old carrier API that still
speaks `java.util.Date`, and the type has to stay. Two bugs came in. The scanning station creates
shipments in a loop and reuses one `Date` object for every scan; at the end of a shift every
parcel of the batch shows the dispatch time of the last one. And since the daily report started
adding two hours to the times it prints, late-dispatch alerts fire for parcels that left on time.

Finish the two `TODO` methods in `src/main/java/practice/Shipment.java`: `keep(...)`, which the
constructor calls with the caller's `Date`, and `handOut(...)`, which `dispatchedAt()` calls.
When you are done, no caller can change a shipment's dispatch moment through a `Date` it passed in
or got back. About two lines in total.
