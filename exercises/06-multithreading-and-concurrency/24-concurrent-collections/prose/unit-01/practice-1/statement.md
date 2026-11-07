The audit service collects events from several collectors. A collector that times out sends its
whole batch again, so one event can reach the importer two or three times, each copy stamped with
the time it was received. Auditors read the log top to bottom and expect each event once, in the
order the events reached us.

`AuditImport` already writes a batch to the log. Finish `firstOfEach(...)` in
`src/main/java/practice/AuditImport.java`: it returns each event of the batch once, as the first
copy that arrived, in the order of first arrival, and leaves the batch it was given unchanged.
About three lines.

Copies of an event are the same event when their ids are equal; nothing else about them has to
match.
