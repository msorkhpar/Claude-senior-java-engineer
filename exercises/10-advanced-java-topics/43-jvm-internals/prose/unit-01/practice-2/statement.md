The subscription shop has two ways in. Most visitors open the price list first; some go straight
to the sign-up form. After a restart last week, the first visitor went straight to sign-up, and
every customer who signed up until the next restart was billed without VAT. In the staging
environment, where the price list is usually opened first, the sign-up form failed instead: new
customers had no tariff at all.

`Tariff` and its `STANDARD` tariff are fine as they are. Finish the two `TODO`s in
`src/main/java/practice/Pricing.java`, both in `Rates`:

- `vatPercent()`: the VAT configured for this deployment (`configuredVat()` reads it);
- `defaultTariff()`: the tariff a new customer starts on, which is the standard tariff.

When you're done, the standard tariff charges the configured VAT and every new customer starts on
it, whichever of the two the shop uses first after a start. About three lines in total.

Each test starts the shop afresh: it loads its own copy of the `Pricing` classes, so their static
fields are set up again, in the order that test uses them.
