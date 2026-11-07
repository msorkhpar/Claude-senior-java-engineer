The monitoring service routes alerts to the on-call pager, to email, or to a dashboard. Two
complaints came in this week: disk alerts below the paging level never arrive anywhere, and
the on-call engineer was woken at night by a read replica, which is supposed to send email
only.

`AlertRouter.routeAll(...)` already groups a batch by channel. Write `channelFor(...)` in
`src/main/java/practice/AlertRouter.java` so that each alert goes where its javadoc says. A
`ReplicaDiskAlert` is a `DiskAlert` too.

About five lines in total.
