The nightly settlement job uploads one payment file per run to the bank, and the bank's gateway
files each upload under the date and time in its name. The bank's specification:

| Cut-off | File name |
|---|---|
| 5 May 2031, 07:05 | `payments_20310505_0705.txt` |
| 9 September 2031, 22:40 | `payments_20310909_2240.txt` |

The date is the cut-off's calendar date, year, month and day; the time is on the 24-hour clock, to
the minute. The gateway refuses a file dated in the future and overwrites a file that has the name
of an earlier one.

Finish `stamp(...)` in `src/main/java/practice/SettlementFiles.java`, so the job names every run
correctly, on every day of the year. About two lines.
