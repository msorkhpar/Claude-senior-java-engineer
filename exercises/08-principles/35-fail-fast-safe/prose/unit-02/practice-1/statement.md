The notification service collects what happens to each user during the day (a paid invoice, a
failed build, a new comment) and a digest job mails each user what is waiting for them, in one
message, every few minutes. Request threads keep adding notifications while the job runs. Support
has two kinds of ticket: users who never got a notification the admin page showed as waiting, and
users who got the same build failure in two digests in a row.

`DigestOutbox` already collects notifications. Write `sendDigests(...)` in
`src/main/java/practice/DigestOutbox.java`: mail every user who has something waiting, one message
each, and return how many users it mailed.

When you're done, every notification reaches its user exactly once, in the run that was going on
when it arrived or in the next one, whatever arrives during a run. About six lines in total.

The tests run the job on one thread and add notifications from inside the mailer, which is where
a request thread's add lands in production.
