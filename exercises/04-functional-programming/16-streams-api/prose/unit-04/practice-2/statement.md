A marketplace shows how many listings carry each tag ("vegan", "handmade", ...). Small shops'
numbers are right. For the large catalogues, which are tallied on a parallel stream, the numbers
change from one page load to the next, run into the billions, some are negative, and now and then
the tally fails with a `ConcurrentModificationException`.

`TagTally.Counts` keeps one count per tag and is not thread-safe. Finish the two `TODO`s in
`src/main/java/practice/TagTally.java`:

- `Counts.addAll(other)`: add another `Counts` into this one;
- `tally(Stream<String>)`: count every tag of the stream into a `Counts`.

The tally must be exact whether its stream is sequential or parallel. About four lines in total.
