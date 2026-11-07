The pricing service converts every price with a rate card that it loads from the rate service.
Two incidents last quarter:

- The rate service was down for ten minutes. The pricing service's health check, which calls
  `RateCard.version()`, failed too, so the orchestrator restarted the pricing pods over and over.
- At one startup the rate service timed out once. It answered again a second later, but every
  price request failed until the pods were restarted by hand.

At startup a few hundred requests arrive together, and the rate service allows one load per
pod.

Finish the `TODO` in `rates()` in `src/main/java/practice/RateCard.java`. When you're done, the
health check answers whatever the rate service is doing, a request whose load fails gets the
rate service's own error and the next request tries again, and requests that arrive together
while the rates are loading wait for that one load. About six lines in total.

Each test loads a fresh copy of `RateCard` through a class loader of its own, so every test
starts as if the pod had just started.
