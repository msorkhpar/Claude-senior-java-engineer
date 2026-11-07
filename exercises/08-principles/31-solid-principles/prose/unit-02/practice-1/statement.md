The shipping service prints a manifest for every van that leaves the depot: what the postage
comes to and when the last parcel arrives. Today it carries `Letter`s and `Box`es. Next quarter
the freight team adds crates and the courier team adds next-day envelopes, each in its own
module, and they won't be editing your code to do it.

Finish the two `TODO`s in `Manifest`, in `src/main/java/practice/Shipping.java`:

- `totalCents(...)`: every parcel's base postage, plus 2 % of the declared value of each insured
  parcel, rounded up to the next cent;
- `deliveredBy(...)`: the day the last parcel is delivered when they all leave on the given day.

When you're done, the manifest prices and dates a parcel kind written after it, by any team,
without a change to `Manifest`. The tests bring kinds of their own. About eight lines in total.
