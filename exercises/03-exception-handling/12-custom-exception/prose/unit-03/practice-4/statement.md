An energy company imports electricity meter readings every night, one file per region. A file
with a line that isn't a reading is skipped and tried again the next night, after someone has fixed
it. The operations team counts the job's log records at `WARNING` and above to decide how many
files need a person in the morning, and the person then needs to find the broken line from the log
alone.

Finish `importFile` and `runNightly` in `src/main/java/practice/MeterImport.java`. The logger is
`MeterImport.LOG` (`java.util.logging`). About ten lines in total.
