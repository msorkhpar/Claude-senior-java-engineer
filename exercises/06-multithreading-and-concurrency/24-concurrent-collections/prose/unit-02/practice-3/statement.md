The search service indexes documents by word: a `ConcurrentHashMap` from each word to the set of
documents that contain it. Indexing threads add and remove documents all day, while search threads
look words up and walk the sets they get back. Support tickets say that a freshly published
document sometimes can't be found by a word it plainly contains, and a search now and then fails
with `ConcurrentModificationException`.

Finish the two `TODO`s in `src/main/java/practice/WordIndex.java`:

- `add(word, documentId)`: record the document under the word;
- `remove(word, documentId)`: take it off again, and drop the word once no document has it.

When you're done, no document is lost however the indexing threads interleave, including one added
at the moment the word's last other document is removed, and a search can walk a word's
documents while they change. About ten lines in total.

The tests make the threads interleave the same way on every run, through the map and the sets the
index is given.
