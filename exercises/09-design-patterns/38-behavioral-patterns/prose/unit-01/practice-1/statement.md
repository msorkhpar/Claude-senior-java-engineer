The checkout page quotes shipping through one `ShippingPolicy` object that every request thread
calls. The carrier's rate table is a remote service, and a lookup can take 300 ms. The version in
production, `ByWeightV1`, gives the right price in every unit test, yet support has tickets from
customers who were quoted the price of somebody else's parcel, and only at busy hours.

Write `quoteCents` in `ByWeight`, in `src/main/java/practice/ShippingQuotes.java`. It gives every
parcel the price `ByWeightV1` gives it when called on its own: billable kilograms (the grams
rounded up, at least 1) times the zone's rate, plus 20 % for the zone `island`. One `ByWeight`
serves every thread at once, and a quote must not wait for another parcel's rate lookup. Leave
`ByWeightV1` as it is. About three lines in total.
