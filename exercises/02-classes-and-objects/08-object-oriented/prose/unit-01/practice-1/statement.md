Every service applies its schema migrations on start. `Migrations.Migration` is the class each
team extends, and each subclass writes the statements of one change. Three incidents this quarter
came from the same place:

- a team overrode `apply` to log timings, dropped the history check, and its change ran twice on
  the next deploy;
- a deploy script called `migrate` on a migration it had loaded and ran the change with nobody
  holding the lock;
- a change that failed halfway left the history locked, and every later deploy refused to start.

Finish `src/main/java/practice/Migrations.java` so that a migration runs its change at most once
per database and only while holding the history's lock, a change that throws is not recorded and
releases the lock, and none of the three incidents can be repeated by a team's subclass or by
code that holds a migration. Teams write their subclasses in their own packages and must keep
being able to. About ten lines in total, most of them in `apply`.
