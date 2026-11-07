The on-call desk turns every alert into an incident with a severity from 1 (worst) to 4. The
response is cumulative: severity 4 only writes the incident log; severity 3 opens a ticket and
does what severity 4 does; severity 2 posts to the incident channel and does what severity 3
does; severity 1 pages the on-call engineer and does what severity 2 does. Within one incident,
the most urgent action comes first. Last week a monitor with a bad config sent severity 0, and
nobody noticed because nothing happened.

`Escalation` already validates the monitor's name. Write `escalate(int severity)` in
`src/main/java/practice/Escalation.java`, as one `switch` statement, so that each severity runs
its actions in that order and a severity outside 1 to 4 throws `IllegalArgumentException`
before any action runs. About a dozen lines in total.
