The support dashboard looks customers up through `CustomerService`. It shows "no such customer"
for `UnknownCustomerException`, "try again later" for `CustomerStoreException`, and sends anything
else to the error tracker as a bug. The repository underneath is JDBC code, and the dashboard team
doesn't want to know about `SQLException`.

Three complaints arrived after the last release. Agents who mistype a customer id are told to try
again later, and do, for minutes. When the database does fail, the log says only "cannot load
customer", and on-call can't tell a timeout from a full connection pool. And a mapping bug
introduced in the repository last week showed up on the dashboard as a database outage, so
nobody opened a bug for it.

Write `find(...)` in `src/main/java/practice/CustomerService.java` so that each of the three
reaches the right place. About eight lines.
