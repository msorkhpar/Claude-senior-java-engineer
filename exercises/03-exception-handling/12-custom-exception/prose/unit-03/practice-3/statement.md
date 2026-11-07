A bank's transfer API rejects transfers outside its per-transfer limits, which differ by customer
tier and change a few times a year. When a business customer calls to ask why a payment was
refused, support pastes the log line into a ticket and on-call answers from it. The ticket system
is read by a hundred people at an outsourcing partner.

Finish `rejection` in `src/main/java/practice/TransferLimits.java`: the message of the exception
that rejects a transfer. From the message alone, on-call must be able to tell what was wrong with
the transfer and find it again, and the ticket must be safe to share. About two lines.
