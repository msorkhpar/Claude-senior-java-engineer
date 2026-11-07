The music app shows a banner above the playlists on its home screen while it prefetches their
tracks: "3 tracks from @tobi, @mira". Users with long playlists see a banner that counts far too
few tracks, and since a few accounts were deleted, some banners read "@null".

`Prefetch.banner(...)` already builds the text. Finish the two `TODO`s in
`src/main/java/practice/Prefetch.java`:

- `trackCount(...)`: how many tracks the player fetches across all the playlists;
- `ownerHandles(...)`: the owners' handles, each once, in the order of their first playlist, and
  nothing at all for an owner whose account is gone.

`Profiles.handleOf(...)` answers with an empty `Optional` for a deleted account. About four lines
in total.
