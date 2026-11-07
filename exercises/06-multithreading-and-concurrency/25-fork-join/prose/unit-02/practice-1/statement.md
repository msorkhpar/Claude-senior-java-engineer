On the first of every month the billing service exports invoices. The export adds up the tax of a
batch of invoices with a parallel stream, and each lookup is an HTTP call to the tax service that
can take seconds. Ops noticed that a service which runs exports all day has a thread count that
only goes up.

`InvoiceExport` already sums the tax. Finish the `TODO` in
`src/main/java/practice/InvoiceExport.java`: `onLookupThreads(...)` runs the lookups in a
`ForkJoinPool` of `lookupThreads` threads and returns their result.

When you're done, nothing of the export is left running once `totalTax` returns, also when a lookup
throws; that exception reaches the caller. About four lines in total.
