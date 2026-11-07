Operators type the start of each batch run as a local date and time in the data centre's zone,
and `BatchStarts` turns it into the moment the job queue receives. Twice a year the operations
team finds a run in the wrong place: in spring a run they set for a time that night started later
than they typed, without anyone being asked, and in autumn the nightly export started while the
hour it reads was still being written.

The team's rules for the nights the clocks change:

- a start at a local time that doesn't happen that night is refused with
  `IllegalArgumentException`, so the operator picks another;
- a start at a local time that happens twice runs at the second one, after the clocks have gone
  back, when the hour's data is complete.

Every other start runs at the time typed. Finish `startOf` in
`src/main/java/practice/BatchStarts.java`. About four lines in total. The data centres include
zones whose clocks change by less than an hour.
