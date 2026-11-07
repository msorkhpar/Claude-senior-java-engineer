The payment client reads its retry settings for each endpoint from YAML. The settings loader
creates a `RetrySettings` through its no-argument constructor and then sets the fields the
document names. Application code also builds settings by hand, mostly with the one-argument
constructor that takes just the endpoint.

Two problems came back from staging. Documents that name only the endpoint gave a client that
never retried. And a client built with `new RetrySettings(...)` from a value typed by an operator
was never recognised by the circuit breaker, which compares endpoints as text, while the same
value passed to the three-argument constructor was.

Finish the two `TODO` constructors in `src/main/java/practice/RetrySettings.java`. When you're
done, every `RetrySettings`, however it was made, has the defaults the class declares for what
nobody gave it, and holds to everything the three-argument constructor promises about its values.
About two lines in total.
