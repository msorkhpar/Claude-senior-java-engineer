The reporting API has two range bugs. A scraper that asks for `?size=4294967346` gets pages of
50 rows and pages through the whole table, where anyone asking for more than the maximum should
get the largest page. And the usage page of a new account, which has no quota yet and has used
nothing, draws its gauge as `NaN` and breaks the chart.

Finish `pageSize(requested)` and `gauge(used, quota)` in `src/main/java/practice/Limits.java`.
The web layer has already parsed `?size=` into a `long`. Each method's comment says what it must
return. About three lines in total.
