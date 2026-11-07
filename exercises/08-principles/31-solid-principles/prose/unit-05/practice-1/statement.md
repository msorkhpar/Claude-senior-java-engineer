The claims service pays approved insurance claims through the bank's API and keeps an audit
line for every decision. Nobody has tested the payout rules since the bank integration went in:
the CI machines have no `BANK_URL`, and the last person who ran the tests on a machine that had
one paid a test claim for real.

The team has settled the shape in `src/main/java/practice/Payouts.java`: `PayoutPolicy` receives
a `Transfers` and an `AuditTrail` in its constructor, and the code that starts the service passes
it a `BankTransfers` and the real audit trail. `Registry` is how older parts of the service find
shared objects; it is being retired.

Finish the constructor, `payOne` and `skip`, so that the tests can run the payout rules with a
fake bank and a list for an audit trail:

- an approved claim with a positive amount is paid and recorded as `paid <claim id> <reference>`;
- any other claim is recorded as `skipped <claim id>`;
- a policy wired up with something missing is refused while the service is starting, not when
  the first claim comes in.

About six lines in total.
