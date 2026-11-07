Partners upload their orders as XML files into the import directory, and the import parses each
one with the JDK's DOM parser. The same disk holds the import host's own configuration, and
partners' systems keep files of declarations next to their orders. A partner's upload account
is a password that more than one person knows.

Partner orders are plain XML: elements, attributes and text. Write `newFactory()` in
`src/main/java/practice/OrderImport.java` so that a plain order is read as before, and no upload
can make the import read another file into the order or load declarations from one. When the parser
refuses a document, the `SAXException` it throws may reach the caller.

About two lines in total.
