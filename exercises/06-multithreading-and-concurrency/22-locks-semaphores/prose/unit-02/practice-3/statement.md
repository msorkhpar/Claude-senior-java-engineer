The transcoding service shares one GPU between jobs. The GPU runs five hardware encoder sessions
at once: a 1080p job needs one, and a 4K job needs four. Three problems are in the backlog. On
the night two 4K jobs arrived together, both stalled until someone restarted the service. The
"try now, else requeue" path sometimes leaves the GPU with fewer free sessions than its gauge
should show. And on busy days a 4K job can sit for an hour while 1080p jobs that arrived after it
keep getting sessions.

Finish the three `TODO`s in `src/main/java/practice/EncoderSessions.java`: `newSessions(...)`,
`reserve(...)` and `tryReserve(...)`. When you're done, a job gets all the sessions it asked for
or none of them, whether it waits, is refused, or is cancelled while waiting; and a waiting 4K job
is not passed by 1080p jobs that arrive after it. About three lines in total.

The tests run jobs on real threads and cancel them through their `Future`, which interrupts them.
