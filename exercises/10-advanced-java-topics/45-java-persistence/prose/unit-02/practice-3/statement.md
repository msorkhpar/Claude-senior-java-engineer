This practice builds on the previous one: `CustomerSession` now gives each request one object
per customer. Requests change those objects, and the changes have to reach the database
without every caller remembering to save. The first attempt wrote every customer the request
had read, and the database team counts four `UPDATE`s for a request that changed one tier.

Finish the three `TODO`s in `src/main/java/practice/CustomerSession.java`:

- `remember(customer)`: keep what the session will compare against; `find` calls it after
  each load;
- `isDirty(customer)`: whether the customer now differs from that;
- `flush()`: write every changed customer with `writeRow`, and return how many it wrote.

A request changes customers through their fields and through their tag lists, may set a field
to the value it already has, and may flush more than once. About ten lines in total.

The tests run against H2 in memory through a connection that counts the `UPDATE` statements
it executes.
