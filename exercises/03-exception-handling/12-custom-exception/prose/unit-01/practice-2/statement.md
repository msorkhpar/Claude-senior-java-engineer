The nightly customer export runs on a fleet of worker JVMs. When an export fails, the job
framework serializes the failure with Java serialization and sends it to the coordinator, which
logs it and schedules a retry for that customer. Since the last release, the coordinator logs
`java.io.NotSerializableException` where the failures used to be, and no failed export has been
retried.

Workers and coordinators are upgraded on different days, so for a while a worker on one release
talks to a coordinator on the other. The coordinators still run the previous release of
`ExportFailedException`, which was declared with `serialVersionUID = 1L` and kept the customer's
id in a `String` field named `customerId`. `Customer` comes from the CRM client library and you
can't change it.

Finish `ExportFailedException` in `src/main/java/practice/Exports.java`: its constructor and
`getCustomerId()`, and whatever its fields need. When you're done, a failure written on either
release reaches the coordinator with its message, its cause and the id of the customer to retry.
About six lines in total.
