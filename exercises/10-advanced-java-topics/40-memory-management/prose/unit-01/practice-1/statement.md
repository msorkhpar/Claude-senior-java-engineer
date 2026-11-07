The community site exports a discussion for the moderation team: every comment in the order the
site shows them, and how deep the longest chain of replies goes. Last week a spam bot answered its
own replies 180,000 times, and the export of that one discussion killed the export worker with a
`StackOverflowError`. The first suggestion in the team channel was to start the whole service
with `-Xss64m`.

`Comment` and `summary(...)` are done. Finish the two `TODO`s in
`src/main/java/practice/Discussion.java`:

- `deepest(...)`: the number of levels in the longest chain of replies, the root being level 1;
- `inOrder(...)`: every comment's id in display order. A comment comes first, then the whole
  thread under its oldest reply, then the thread under its next reply, and so on.

When you're done, any discussion exports on the worker's ordinary threads, the bot's included,
with no change to the service's stack size. About fifteen lines in total.

The tests run each export on a thread with a 1 MB stack.
