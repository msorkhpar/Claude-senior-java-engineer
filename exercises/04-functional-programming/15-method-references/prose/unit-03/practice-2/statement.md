The operations notifier turns events into short notices. Each event names a template and
carries one payload: a single value, an `Object[]` from the JSON decoder when the event has
several values, or an `int[]` from the metrics feed. The notifier takes its renderer as a
`BiFunction<String, Object, String>`, and until now it was configured with `String::format`.
Notices with one value went out fine. The first event with two values failed with
`MissingFormatArgumentException`, and the on-call engineer never heard about the cache that had
restarted four times.

Finish `render(String template, Object payload)` in `src/main/java/practice/Notices.java`, which
the notifier now uses as `Notices::render`. Each value of the payload fills the next format
specifier of the template, whatever kind of payload the event carries. About three lines in
total.
