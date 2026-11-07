The warehouse scans forty thousand shipments an hour, and every scan goes through
`ShipmentChecks` before a label is printed. A profile of the scanner shows most of the checker's
time in `describe`, which formats every parcel of a shipment, and in building not-found errors,
each with its stack trace. Yet fewer than one shipment in ten thousand lacks a carrier, and
unknown ids are rarer still.

Finish `carrierOf` and `find` in `src/main/java/practice/ShipmentChecks.java`. A shipment
without a carrier fails with a `NullPointerException` whose message is its description, and an
unknown id with the exception `notFound` builds for it; the shipments and ids that pass must cost
nothing extra. About two lines in total.
