The mail service checks every outgoing message against its delivery rules: which recipient domains
are blocked, and the largest message each domain accepts. Dozens of send threads read the rules for
every message, thousands of times a second, and must never wait for a lock. The admin API changes
the rules when a domain starts bouncing or a partner asks for a smaller limit.

Support has two complaints. During a busy hour of rule changes, a send thread that had read the
rules for a message saw them change while it was still checking that message. And after a block,
one send thread went on sending to the blocked domain for half an hour.

Finish the three `TODO`s of `Store` in `src/main/java/practice/DeliveryRules.java`: `block`,
`unblock` and `setMaxSizeKb`. Changes come from one admin thread at a time. When you're done, every
change applies to the rules read after it, and rules a send thread has already read keep answering
as they did when it read them, so a message is never checked against rules that change under it. You may
change how `Store` holds the rules. About ten lines in total.
