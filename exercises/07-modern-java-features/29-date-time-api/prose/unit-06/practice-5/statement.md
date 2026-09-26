A letter is dated the way its reader's language writes a full date. Write
`LetterDate.format(LocalDate date, Locale reader)`: the date in the **reader's
own full-date style**. Each language has its own words, its own **order** and
its own punctuation, so a US pattern with translated words is not enough.

The server's own default locale must play **no part**.

| date | reader | answer |
|---|---|---|
| `2024-03-15` | `Locale.US` | `Friday, March 15, 2024` |
| `2024-07-04` | `Locale.US` | `Thursday, July 4, 2024` |
| `2024-03-15` | `Locale.FRANCE` | `vendredi 15 mars 2024` |
| `2024-03-15` | `Locale.GERMANY` | `Freitag, 15. März 2024` |
