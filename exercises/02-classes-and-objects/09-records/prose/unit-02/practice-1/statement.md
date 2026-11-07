The quoting service caches each shipping rate under the `Shipment` it was quoted for. Since a
new client went live, the cache keeps growing while its hit rate falls, and support has a ticket
about a shipment that was quoted twice at different prices. The new client builds each shipment
from one `HashMap` of parcels, which it keeps filling for the next request.

Finish the two `TODO`s in `src/main/java/practice/RateCache.java`:

- the compact constructor of `Shipment`: once built, a shipment keeps the parcels it was given,
  whatever later happens to the caller's map or to the map `parcels()` returns;
- `withParcel(id, grams)`: a new shipment with one more parcel, leaving this one as it is.

When you're done, a cached shipment is found again however the code around it treats its maps.
About four lines in total.
