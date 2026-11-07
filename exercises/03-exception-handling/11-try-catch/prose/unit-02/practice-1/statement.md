The warehouse books a shipping label for every parcel through the carrier's API. Addresses come
from customers' typing, so the carrier sometimes rejects one, and `ship(...)` then hands the parcel
to `bookNormalized(...)`, which tries once more with the address cleaned up.

During last week's carrier incident, the packing stations showed a stack trace instead of the usual
"retry later" for every parcel that had needed a second attempt. And a parcel whose address was
wrong in both forms must go to the address-review queue, where a person fixes it; it must not
come back every ten minutes.

`ship(...)` already books, and already answers a carrier failure on the first attempt. Finish the
`TODO` in `src/main/java/practice/LabelBooking.java`:

- `bookNormalized(...)`: book the parcel with `normalize(address)` and return what `ship(...)`
  returns: `"booked: "` and the tracking number, `"review: "` and the carrier's reason when the
  address is rejected again, or `"retry later: "` and the message when the carrier fails.

`ship(...)` never throws a carrier failure at its caller. About six lines in total.

The tests use a scripted carrier, with addresses and failures of their own.
