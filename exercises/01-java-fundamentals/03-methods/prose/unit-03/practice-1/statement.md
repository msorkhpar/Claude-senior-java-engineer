The mail client keeps each folder as a list of messages, and each screen redraws from the list it
holds. The folder operations in `Folders` take those lists and change them: `deleteFrom` already
works, and the inbox screen shows the result as soon as it returns.

"Archive all" is not written yet. Finish `archiveAll(...)` in
`src/main/java/practice/Folders.java`. When you're done, the inbox screen shows an empty inbox,
the archive screen shows the moved messages after the ones it already had (also when the archive
was empty), and the method returns how many messages moved. About three lines in total.
