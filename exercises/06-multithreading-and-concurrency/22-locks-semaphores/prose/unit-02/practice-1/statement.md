The checkout scores every card payment with a fraud-scoring vendor. The contract allows this
client a fixed number of calls in progress at once; when no session comes free within a short
wait, the call is refused with `Busy` and checkout falls back to its offline rules.

The client has been in production for a month, and the incidents have gone both ways. After a
morning of vendor errors, every checkout thread sat waiting for a session while the vendor saw no
traffic at all. Two weeks later, during a busy evening when many calls were refused and many
checkouts were abandoned while waiting, the vendor reported that the client had gone over its
limit and throttled it.

Write `score(...)` in `src/main/java/practice/ScoringClient.java`. When you're done, the client
never has more calls in progress than the contract allows and never fewer sessions than it is
owed, whether a call is scored, fails at the vendor, is refused, or is cancelled while it waits.
About six lines in total.

The tests run real threads against a vendor whose calls stay open until the test lets them finish.
A cancelled checkout is cancelled through its `Future`, which interrupts the waiting thread.
