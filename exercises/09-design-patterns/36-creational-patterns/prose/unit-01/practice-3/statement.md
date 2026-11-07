The shop's checkout and refund services used to count into `Metrics.getInstance()`. Now the
hosting platform runs one shop per tenant, several in one JVM, and each tenant's dashboard showed
every tenant's orders. The team has removed the static accessor: `Checkout` and `Refunds` take a
`Metrics` in their constructors, and `Shop.start()` is where the platform builds a shop when a
tenant's shop starts.

Finish the three `TODO`s in `src/main/java/practice/Shop.java`: the two constructors and
`start()`. When you're done, each running shop's dashboard (`Running.metrics()`) shows exactly
what that shop's own checkout and refunds counted. About five lines in total.
