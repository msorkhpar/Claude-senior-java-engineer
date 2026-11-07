A shared workspace keeps its activity feed oldest first, and two consumers want it newest first.

- The **dashboard** asks once, when it opens, and keeps the list it gets. Users complain that it
  never shows anything new until they reload. Worse, a bug in a dashboard widget once added an
  event to the list it was given, and that event turned up at the start of the real feed.
- The **daily digest** e-mail picks the newest few events, then is rendered and sent minutes
  later. Its list must not change in the meantime.

Finish `live()` and `latest(n)` in `src/main/java/practice/ActivityFeed.java`. About two lines in
total.
