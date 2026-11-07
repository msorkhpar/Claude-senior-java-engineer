The notification service keeps the push tokens of each user's devices in a `DeviceTokens` store.
Request threads register a token when an app starts, revoke it on logout, and rotate it when the
push provider renews a device's token. The sender takes a `snapshot()` for every notification.
Since the store went live, some users get each notification twice, and support has a log where a
renewed token shows up twice in a snapshot.

Finish the two `TODO`s in `src/main/java/practice/DeviceTokens.java`:

- `register(token)`: add the token unless it is already registered, and say whether it was added;
- `rotate(oldToken, newToken)`: replace the old token by the new one, only if the old one is
  registered and the new one isn't; when it can't, change nothing and return `false`.

When you're done, a token is stored once however many threads register or rotate onto it at the
same moment, and a rotation that returns `false` has left the store as it was. About ten lines in
total.

The list inside the store is a `Collections.synchronizedList`. The tests hand the store a list of
their own through the package-private constructor, which holds a thread right after a lookup with
`contains` or `indexOf`, so look tokens up with one of those.
