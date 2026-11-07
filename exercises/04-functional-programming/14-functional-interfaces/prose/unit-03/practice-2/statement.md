The chat service shows a green dot next to everyone who is online. A user counts as online while at
least one of their connections (phone, laptop, another tab) is open. Connection events arrive on many
threads at once, and the gateway sometimes sends a disconnect twice, or for a user it never reported
as connected.

`Presence` keeps an open-connection count per user in a `ConcurrentHashMap`, and `online()` lists
the users that have an entry. Finish the two `TODO`s in `src/main/java/practice/Presence.java`:
`connect(user)` and `disconnect(user)`, each one call on the map. When you're done, a user whose
last connection closes disappears from `online()`, and a disconnect nobody can match leaves the map
as it was. About two lines in total.
