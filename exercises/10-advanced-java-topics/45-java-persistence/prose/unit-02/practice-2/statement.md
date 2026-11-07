In the account service, one request reads the same customer in three places: pricing, the
address check and the profile update. Each place calls `find(id)` and gets its own copy, so
the profile update changes the tier on one copy while pricing goes on using the old tier from
another. The database log also shows the same `select` three times per request.

`CustomerSession` is opened for one request and dropped at its end. Write `find(long id)` in
`src/main/java/practice/CustomerSession.java`, and any field it needs, so that within one
session every caller asking for an id gets the same object, read from the database once,
while a session opened for another request gets objects of its own. `load(id)` already reads
a row. About six lines in total.

The tests run against H2 in memory through a connection that counts the statements it
executes.
