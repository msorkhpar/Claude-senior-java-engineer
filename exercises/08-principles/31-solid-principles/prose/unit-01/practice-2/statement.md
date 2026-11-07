A ferry operator sells tickets at a machine on the quay and, from next month, in a web shop.
Finance owns the fares, and its rules are written as small step classes in
`src/main/java/practice/Fares.java`: the base fare, a group discount, a peak surcharge. The ticket
machine puts them together and has charged correctly for years.

Two things are wanted before the web shop opens. The web shop must quote exactly what the machine
will charge for the same trip, or customers dispute the difference at the quay. And finance wants
the group discount's threshold to change with the season, through the `FarePolicy` it is
building: a machine and a web shop given a policy must both charge that policy's fares.

Finish the two `TODO`s, `FarePolicy.price(...)` and `WebShop.quote(...)`, and change anything else
in the file you need to. About six lines in total.
