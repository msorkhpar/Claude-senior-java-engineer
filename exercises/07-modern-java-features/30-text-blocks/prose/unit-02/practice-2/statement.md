The billing job sends a payment reminder for every unpaid invoice, and a copy of each letter goes
to the payment portal, which reads the invoice line and expects the amount as digits, a dot and
two decimals. The job runs on servers in Dublin and in Frankfurt. Two incidents came in this month:

- every letter sent from Frankfurt was rejected by the portal;
- the job stopped halfway through one night's run, on a customer whose account manager had left a
  note on the invoice.

`ReminderLetter` already holds the letter's template, `LETTER`. Write `render(Invoice)` in
`src/main/java/practice/ReminderLetter.java` so that the letter is the same wherever the job runs,
and any note the account manager writes appears as its own last line, exactly as written. About
five lines in total.
