Site inspections are filed as evidence. Last week an auditor found an inspection of dock 7 dated
1 January 1970, and another whose list of defects no longer matched the inspector's notes. The
weekly report rounds the dates it is given, and the compliance export rewrites defect names in
the lists it reads.

`Inspection` keeps its date as a `java.util.Date` and its defects as a `String[]`, and its
constructor already keeps copies of what it was given. Finish the two getters in
`src/main/java/practice/Inspection.java`, `getInspectedAt()` and `getDefects()`, so that nothing a
caller does with what they return changes a filed inspection. About two lines in total.
