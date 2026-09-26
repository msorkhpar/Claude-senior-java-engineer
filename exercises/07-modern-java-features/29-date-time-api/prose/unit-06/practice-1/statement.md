Every line of a service's log starts with a timestamp such as
`2024-03-15 14:30:45`. Write `LogStamp.format(LocalDateTime t)`, which prints `t`
that way:

- the four-digit year, the month and the day of the month, joined by `-`;
- a space;
- the hour, the minute and the second, joined by `:`;
- every field but the year padded to two digits;
- no fraction of a second, even when `t` has one.

The hour is on the **24-hour clock** (`14`, and `00` just after midnight), and
the year is the **calendar year** of the date, on every day of the year.

| t | answer |
|---|---|
| `2024-03-15T09:03:07` | `2024-03-15 09:03:07` |
| `2024-03-15T09:03:07.5` | `2024-03-15 09:03:07` |
| `2024-07-15T09:41` | `2024-07-15 09:41:00` |
| `2024-03-15T14:30:45` | `2024-03-15 14:30:45` |
| `2024-03-15T00:15` | `2024-03-15 00:15:00` |
| `2024-12-30T23:59:59` | `2024-12-30 23:59:59` |
