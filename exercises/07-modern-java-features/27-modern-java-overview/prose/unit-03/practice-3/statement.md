The payments platform runs forty services, each with a start script full of `-XX` options that
people added over ten years. The services are moving from Java 11 to the next LTS release in one
step. The platform team keeps an inventory of HotSpot options: for each, the release in which it
was deprecated, made obsolete, and expired, with `null` where that hasn't happened yet. Before any
service moves, a check reads its options against the inventory and reports what the new JVM will
do with them.

`StartOptionsCheck` already reads the options and builds the report. Finish the two `TODO` methods
in `src/main/java/practice/StartOptionsCheck.java`:

- `stage(...)`: where an option is in its life on a given release;
- `refusesToStart(...)`: whether an option in that stage stops the JVM from starting.

The report must tell the team which options only cost a warning and which would take a service
down on its first deploy. About six lines in total. The tests use a made-up inventory, not the
history of real options.
