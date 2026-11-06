Each of the billing service's queues has one consumer, kept running by a `Supervisor` that a
scheduler calls every five seconds. A consumer can die on a message it can't handle, and the
supervisor is there to bring it back. Two incidents in one week: after the first refund in an
unsupported currency, the refunds queue stopped for good and the scheduler logged an exception
from `check()` every five seconds. And on a quiet Sunday the invoices queue had nine consumers,
each taking messages from the same queue.

Finish `check()` in `src/main/java/practice/Supervisor.java`. `newWorker()` builds a thread for the
consumer with the right name.

When you're done, the first check starts the consumer, a consumer that died is working again
after the next check, and a queue never has more than one consumer. About four lines.
