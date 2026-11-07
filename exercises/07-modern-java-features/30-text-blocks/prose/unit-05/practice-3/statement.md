The changelog page publishes a feed of release notes, one XML `<entry>` per release, built from a
text block in `FeedEntry`. Release managers type the titles and their own names. Since the note
"Fixes for Q&A and R&D" went out, feed readers show an error instead of the feed, and a title that
mentioned an HTML tag came out as something else.

Finish the `TODO` in `src/main/java/practice/FeedEntry.java`: `escape(text)` returns the text so
that an XML parser reads it back exactly as typed, inside an element and inside an attribute value
written in double quotes.

About four lines. The tests parse every entry with the JDK's own XML parser.
