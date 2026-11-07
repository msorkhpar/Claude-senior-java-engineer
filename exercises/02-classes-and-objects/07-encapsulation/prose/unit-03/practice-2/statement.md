The authorizer builds one `AccessPolicy` at startup from a table the config loader produces,
and asks it on every request. Last week an auditor exported customer data the policy had never
granted. The loader reuses its lists when it reloads, and a plugin ships a class that extends
`AccessPolicy`.

Finish `src/main/java/practice/AccessPolicy.java` so that a policy, once built, answers the same
for as long as it lives: whatever its caller later does with the map and lists it passed in, or
with what `actionsFor` and `roles` return, and whatever class a caller passes off as an
`AccessPolicy`. Requests for a role or an action the table doesn't give are still refused, and
`actionsFor` keeps the order the table gave.

About five lines in total, in `frozenCopy` and the class declaration.
