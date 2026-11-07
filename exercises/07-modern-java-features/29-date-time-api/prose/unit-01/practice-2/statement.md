The loyalty system is moving customers' birth dates to a `DATE` column. The old table has three
`int` columns, `year`, `month` and `day`, which a service from 2003 filled straight from
`Date.getYear()`, `Date.getMonth()` and `Date.getDate()`. Some rows were typed in by hand over the
years and name days that never existed; the migration must set those aside for a person to fix,
not guess a date for them. The old reports keep reading the table until the switch, so new rows
must still be written in the old form.

`migrate(...)` already loops over the rows. Finish the two `TODO` methods in
`src/main/java/practice/BirthDates.java`: `toLocalDate(...)`, from the old columns to a
`LocalDate`, and `toColumns(...)`, the other way. About two lines in total.
