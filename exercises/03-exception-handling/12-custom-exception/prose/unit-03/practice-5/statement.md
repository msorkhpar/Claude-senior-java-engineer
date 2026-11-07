An ordering service's mobile app decides what to show from the API's error responses. The order
module's developers improve their exception messages often, to make the logs easier to read, and
those messages hold customer ids and the names of internal risk rules. After the last release the
app started showing "Something went wrong" for out-of-stock items, and a security review flagged
a response that named a risk rule.

Finish `toResponse` in `src/main/java/practice/OrderApi.java`, using the statuses its comment
documents. About five lines.
