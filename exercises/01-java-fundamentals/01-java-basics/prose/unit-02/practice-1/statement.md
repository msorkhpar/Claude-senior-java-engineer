A variable of a reference type holds a **reference** to an object, not the
object. Copying the variable copies the reference, so two variables can point
at one object: change it through one and the other sees the change (aliasing).

`Playlist` holds a name and a `List<Song>`, and a `Song`'s title can change.
Write its two copy methods:

- `shallowCopy()` returns a new `Playlist` that **shares** this playlist's list:
  a song added to the original's list is in the copy's list too.
- `deepCopy()` returns a new `Playlist` with **its own** list and **its own**
  `Song` objects: nothing done to the original afterwards, to its list or to one
  of its songs, reaches the copy.

Both copies are new `Playlist` objects with the same name and the same song titles.
