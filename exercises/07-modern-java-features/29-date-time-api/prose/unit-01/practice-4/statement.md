A card processor settles with an exchange in Tokyo and totals its payments per business day of
that exchange. The ledger API it reads still returns `java.util.Date`. Since part of the fleet moved
to a data centre in São Paulo, the totals from those servers disagree with the exchange's for
payments made in the evening there. One older caller passes, instead of a payment moment, the
settlement day it read from a `DATE` column with `ResultSet.getDate`; its report has been crashing,
and once fixed it must show the same day the column holds, on servers in any zone.

Finish `businessDay(...)` in `src/main/java/practice/SettlementReport.java`. About four lines.
