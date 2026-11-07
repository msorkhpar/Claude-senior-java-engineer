The deploy tool produces two pieces of text, and both have bitten the team.

- The **crontab** it installs on every host has a header line and three jobs. The host's installer
  rejects a crontab unless every line, the last one included, ends with a line break, and last
  week it rejected ours.
- For each request to the artifact gateway, the tool signs a **string to sign**: the method, the
  path and the date, one per line. The gateway computes its HMAC over exactly those characters,
  with nothing after the date, and answers `403` when the two signatures differ.

Write both methods in `src/main/java/practice/DeployTexts.java` as text blocks; the javadoc of
`crontab()` lists its lines. The tests compare the exact characters each method returns, with
requests of their own. About ten lines in total.
