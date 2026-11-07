A party playlist service lets guests add tracks from their phones. The host can lock the playlist
while the DJ reorders it, and the venue's screens get a view that only displays it. Every service
of the company answers failures through the same handler, `Playlist.Api.status`, which nobody on
your team may change.

Guests complain: the app tells them "server error" when they ask for a track that isn't there,
and tells them their request is malformed when they add a song during a reorder, so they never
try again.

In `src/main/java/practice/Playlist.java`, finish the three methods that create the exception for
each failure, and decide what `NoSuchTrackException` extends. The mobile app catches
`NoSuchTrackException` by name, so it stays. The client should learn from the status whether to
fix its request, try again later, or stop trying. About four lines in total.
