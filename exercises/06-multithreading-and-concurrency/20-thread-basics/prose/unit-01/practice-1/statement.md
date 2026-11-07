The support service polls a shared inbox with `InboxPoller`. The class was written as a thread of
its own, and the older deployments still create it and call `start()`. Last month the service got
a background pool, and the poller is now also handed to it with `pool.execute(poller)`. Since
then, on the deployments that use the pool, every deploy waits for the pool until its timeout
runs out, and when on-call looks for the poller in a thread dump, the name in its log lines
matches no thread at all.

The polling loop already works. Finish the two `TODO`s in
`src/main/java/practice/InboxPoller.java`:

- `stopRequested()`: whether the poller has been asked to stop;
- `workerName()`: the thread name its log lines carry.

When you're done, the poller behaves the same whichever way it runs: started on its own and
interrupted, or run by the pool and shut down with `shutdownNow()`, it stops, and its log lines
name a thread you can find in a dump. Two lines in total.

The tests run the poller both ways, on its own thread and on a real `ExecutorService`.
