The editor's **Open Recent** menu has three bug reports. A file the user opens again stays where
it was in the list instead of coming to the top. On a fresh install, the editor crashes at startup
while looking for a file to reopen. And once the menu is full, the file the user has just opened
is the one that disappears.

`RecentFiles` keeps the paths in a `LinkedHashSet`, oldest first, and `menu()` already shows them
most recent first. Finish the two `TODO`s in `src/main/java/practice/RecentFiles.java`:

- `open(path)`: the opened file becomes the most recent one, and the menu holds at most
  `capacity` files;
- `mostRecent()`: the file to reopen on startup, or an empty `Optional` when there is none.

About four lines in total.
