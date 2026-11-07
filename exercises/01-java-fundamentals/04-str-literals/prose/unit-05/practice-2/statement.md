Each invoice-export worker formats every invoice in one `StringBuilder` of its own, kept in
`ScratchBuffer`, and the workers live as long as the service. Once a month a customer's invoice
runs to tens of thousands of lines. Since then, monitoring shows the export service's heap
staying high for weeks, and the heap dump finds each worker holding a buffer of several hundred
kilobytes, all of it empty.

Write `acquire()` in `src/main/java/practice/ScratchBuffer.java`. When you're done, each invoice
starts from an empty builder; after a huge invoice the worker holds no more than `KEEP_LIMIT`
characters of room; and ordinary invoices, including the next one after a huge one, start with
`NORMAL_CAPACITY` ready. About five lines.
