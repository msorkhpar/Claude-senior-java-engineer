The issue tracker's search form has an assignee field, a minimum priority and a row of label
boxes. An issue is found when it meets every field the user filled in; for the labels, carrying
any one of the ticked labels is enough. The service builds one `Predicate<Issue>` per filled-in
field and combines them in `matching(...)`.

Finish `src/main/java/practice/IssueSearch.java`:

- `allOf(...)`: one predicate that holds when every criterion in the list holds;
- `anyLabel(...)`: one predicate that holds when an issue carries at least one of the labels;
- the label criterion in `matching(...)`.

Users search with every combination of blank and filled-in fields, the completely blank form
included, which they use to see the whole board. About six lines in total.
