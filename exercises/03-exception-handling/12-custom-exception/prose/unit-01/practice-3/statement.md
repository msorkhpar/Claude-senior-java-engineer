The parcel router tries its routing rules in order, and the first rule that applies picks the hub.
A rule that doesn't apply throws `NoRouteException`, and `route()` catches it and tries the next
rule; a typical parcel is refused by forty rules before one applies. The router is the busiest
service in the depot, and a profile of it shows 40% of its CPU time inside `NoRouteException`'s
constructor. When no rule applies at all, the failure goes to the caller and into the error log,
and the on-call engineer has to be able to see where it came from and which rule refused last.

Finish the two `TODO`s in `src/main/java/practice/Routing.java`:

- the constructor of `NoRouteException`;
- `unroutable(...)`, the exception `route()` throws when no rule applies.

About three lines in total.
