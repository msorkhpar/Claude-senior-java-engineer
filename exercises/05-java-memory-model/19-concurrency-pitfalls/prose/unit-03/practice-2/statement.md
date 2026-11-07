This practice continues the previous one: `Store` now replaces the rules on every change, and send
threads read them without a lock. Each send thread takes a batch of up to 5,000 queued messages at a
time and hands the allowed ones to the mail transfer agent through a `Transport`.

When a partner domain asks the service to stop, the admin blocks it, and the partner expects the
mail to stop within a message or two. The batch that was sending at the time must not finish the
domain's queue first, and a sender that has been running since the morning must not keep the rules
it started with.

Finish `sendAll(...)` of `Sender` in `src/main/java/practice/DeliveryRules.java`. A sender gets its
rules from a `Supplier<Rules>`, which is `store::current` in production. When you're done, it sends
each message the rules allow, in order, and returns how many it sent; a change to the rules applies
from the next message on; and a message is never let through by one version's block list together
with another version's size limit. About seven lines in total.

The tests change the rules from inside the transport, between two messages, and hand the sender
suppliers that return a different version on every call.
