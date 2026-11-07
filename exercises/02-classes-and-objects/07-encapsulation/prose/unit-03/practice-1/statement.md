At 03:11 an engineer saved an alert rule with a window of `0s`. The API answered
`400 Bad Request`, and the engineer fixed the typo. At 03:12 the evaluator's thread died with a
`NullPointerException`, and no alert fired for the rest of the night.

Every rule lives in a `RuleBook`, which the evaluator walks once a minute. Make
`src/main/java/practice/AlertRule.java` refuse, with `IllegalArgumentException`, any rule the
evaluator can't run: a missing or blank metric, a threshold that isn't a finite number, or a
missing window or one shorter than a minute. A rule that was refused must never be walked by
the evaluator, while every valid rule still is.

About six lines in total: the `check` method, and whatever else in the class it takes.
