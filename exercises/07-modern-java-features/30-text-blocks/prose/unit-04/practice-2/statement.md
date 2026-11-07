The team's test generator records what a service returns and writes it into a generated JUnit
test as a Java text block, so the next run is compared with exactly that string. Most generated
tests work. A few don't compile, and a few fail on the very first run although the service hasn't
changed: the string javac reads back from the block isn't the one that was recorded.

`FixtureWriter.textBlock(value)` in `src/main/java/practice/FixtureWriter.java` already writes the
delimiters, the indentation and the line breaks between lines. Finish its `TODO`, `line(text)`:
what to write for one line of the value so that javac reads that line back exactly. Leave alone
whatever needs no change, so the generated fixtures stay readable. Recorded values hold printable
ASCII characters, spaces, carriage returns and line feeds.

When you're done, any such value written by `textBlock` compiles, and javac reads it back
unchanged. About twelve lines in total.

The tests compile each generated block with the JDK's own compiler and compare the value it holds.
