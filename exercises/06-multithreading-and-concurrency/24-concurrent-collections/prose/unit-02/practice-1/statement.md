The public API sells calls in packs. Billing tops a client up through `grant`, and every request
thread calls `tryUse` before it serves a call. In last month's traffic spike two things went
wrong: clients who bought a pack at the same moment as an automatic renewal got only one of the
two, and clients with a single call left were served two or three calls for it.

The quotas already live in a `ConcurrentHashMap`. Finish the two `TODO`s in
`src/main/java/practice/Quotas.java`:

- `grant(client, calls)`: add the calls to what the client has left;
- `tryUse(client)`: take one call if there is one, and say whether it did.

When you're done, every top-up counts however many arrive together, a client's last call is
served once, and the quota never goes below zero. About eight lines in total.

The tests make two calls interleave the same way on every run, through the map the class is given.
