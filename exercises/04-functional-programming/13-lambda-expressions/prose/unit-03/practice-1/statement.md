A web shop's checkout decides which parcels may ship next-day and what a parcel costs. The team
declared its own `Rule` and `Adjustment` interfaces in `src/main/java/practice/Checkout.java`,
so the code reads in the shop's words, but nobody has written their combining methods yet:
`Rule.and`, `Rule.or` and `Adjustment.then` all throw.

Write those three methods. When you're done:

- `nextDay()` turns down a parcel whose postcode is missing instead of failing on it;
- every rule in a chain is asked at most once, in the order the chain was written, and only while
  the answer is still open, because some of the shop's rules call a slow address service;
- `price(percentOff, courierFee)` charges the courier fee in full on top of the discounted goods.

About four lines in total.
