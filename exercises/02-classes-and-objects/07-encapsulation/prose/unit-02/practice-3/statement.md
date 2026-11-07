The issue tracker's label filter lists `regression` three times: people typed `Regression`,
` regression` and `REGRESSION `, and each became its own label. The audit log has the same
problem: it shows labels with the spacing and case they were typed with.

`Labels.Label` is a record. Finish the two `TODO`s in `src/main/java/practice/Labels.java`:

- `normalize(...)`: the typed name without surrounding spaces, in lower case;
- the record `Label`, so that every spelling of a label is one label: the same name, the same
  entry in a set, and the same text in the log.

About two lines in total.
