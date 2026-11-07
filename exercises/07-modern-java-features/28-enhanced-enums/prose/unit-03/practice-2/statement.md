At checkout, the customer is shown the way of paying that costs the merchant least. Merchants
choose what they offer: some switch off the wallet or the bank transfer, and some have signed a
rate of their own that sales loads from the contract. Support has two tickets: a merchant without
bank transfers whose customers are told to pay by bank transfer, and a merchant whose negotiated
card rate is never offered even though it is the cheapest.

Finish the `TODO` in `src/main/java/practice/Checkout.java`:

- `cheapest(offered, amountCents)`: the rule with the lowest fee for that amount, out of the rules
  this merchant offers.

When you're done, checkout picks among exactly what the merchant offers, standard method or
negotiated rate. About five lines in total.
