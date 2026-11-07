The integration tests of the catalogue service kept their expected responses as text blocks, two
hundred of them, and the test classes became unreadable. The team moved each block's body into a
fixture file, byte for byte: everything after the line of the opening `"""`, up to the closing
`"""`. Some bodies hold escapes, some hold Unicode escapes such as `\u00e9` that their authors used
to keep the sources in ASCII, and the files are checked out with CRLF line endings on Windows.

A fixture is only useful if it loads as exactly the string the text block had. Finish the two
`TODO`s in `src/main/java/practice/FixtureLoader.java`:

- `unicodeEscapes(text)`: the text with each Unicode escape replaced by its character, as javac
  does when it reads a source file;
- `load(body)`: the string javac made of a text block with this body.

When you're done, every fixture loads as the value javac gave its block, on Linux and on Windows.
About fifteen lines in total.

The tests compare with values that javac 21 produced from the same bodies.
