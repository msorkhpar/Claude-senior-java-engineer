The ops team watches disk usage through a monitoring feed. A `DiskAlarm` raises an alarm for each
reading of its volume at or above its limit. The feed sends a new listener the latest reading of
every volume as soon as it subscribes, so an alarm started while a disk is already at 97 % should
raise at once.

Two complaints came in about the old version: an alarm started on a full disk stayed silent until
the next reading, and an alarm whose limit was typed as `0` was refused with an error, yet the
feed's listener count kept growing with every such attempt.

Finish the private constructor and `watch(...)` in `src/main/java/practice/DiskAlarm.java`. When
you're done, `watch` either returns an alarm that hears every reading from the latest ones on, or
throws `IllegalArgumentException` for a limit outside 1 to 99 and leaves the feed as it was. About
six lines in total.
