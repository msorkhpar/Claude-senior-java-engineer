The plant's nightly temperature report stops at the first sensor that has not been calibrated
yet. The `Reading` record was written by another team: its `celsius()` refuses to hand out the
value of an uncalibrated sensor, and also refuses a temperature below absolute zero, which only
a broken feed can produce. The report prints nothing that night, and the morning shift has no
temperatures at all.

Finish `safeLine(...)` in `src/main/java/practice/SensorReport.java`:

- an uncalibrated sensor gets the line `<sensor> uncalibrated`, for example `boiler-9 uncalibrated`,
  and the report goes on with the next event;
- any other failure, a temperature below absolute zero included, still fails the report, and the
  reason it gives is the record's own.

Leave `Reading` and `line(...)` as they are: other reports use them. About six lines in total.
