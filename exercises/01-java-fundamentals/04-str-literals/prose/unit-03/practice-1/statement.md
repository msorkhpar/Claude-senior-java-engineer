The notification service sends long text messages as several SMS segments, and the phone joins
them back together. A Unicode segment holds a fixed number of UTF-16 code units. Support has two
complaints: some messages arrive with a `?` where an emoji should be, and the gateway rejects
others as oversized.

`SmsSegments.segments(...)` already splits a message. Finish the `TODO` in
`src/main/java/practice/SmsSegments.java`: `cut(text, units)` returns the index where the first
segment of `text` ends.

When you're done, every segment fits the segment size, the segments joined give back the
original text, and each segment can be shown on its own, whatever the message holds. About four
lines in total.
