An API gateway reads two things from its settings, which an operator can reload at any time
without a restart. Both readers are wired once and then called again and again:

- the **rate limiter** is built when the gateway starts, sometimes before the first settings have
  been loaded, and checks its limit before every request. After a reload, operators expect the
  next window to use the new limit.
- an **export run** stamps the region into the header of every file it writes. Auditors expect
  all headers of one run to name the region that was in force when the run started, even if
  someone reloads halfway through.

`SettingsHolder`, `RateLimiter` and the two builders in `src/main/java/practice/LimiterWiring.java`
already work. Finish the two `TODO` methods, `liveLimit(...)` and `regionAtStart(...)`, so that
both expectations hold. About three lines in total.
