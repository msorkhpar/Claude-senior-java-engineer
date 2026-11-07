Partners upload order documents, and the upload service checks each one against the partner
schema before accepting it. A `Checker` compiles the schema when it is built, which costs a lot of
CPU and memory, and it keeps state between calls, so one thread at a time may use it. The service
used to run uploads on a pool of 16 platform threads and kept one checker per pool thread. It now
runs every upload on a virtual thread of its own, and at the last partner deadline the instances
spent more CPU building checkers than checking documents.

Operations has settled on a limit: the service may hold at most `max` checkers. Finish the two
`TODO`s in `src/main/java/practice/SchemaChecks.java`:

- `borrow()`: hands out a checker for one upload;
- `check(document)`: checks one document on the calling upload's thread.

When you're done, any number of uploads in flight build at most `max` checkers, an upload that
finds them all busy waits for one, no checker is ever used by two uploads at once, and a document
that makes its checker throw doesn't leave the service short of a checker. About twelve lines in
total, including the fields you add.

The tests send each upload on a virtual thread of its own, count the checkers built, and hold
checks in progress to see who uses which checker.
