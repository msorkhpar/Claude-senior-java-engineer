The service's tracing library records a span for every database call and every HTTP request,
and each span asks `AttributeKey.of(...)` for the keys of its attributes. An allocation profile
of the checkout service shows `AttributeKey` among the top three allocated classes: hundreds of
thousands of keys a minute, nearly all of them for the four names in `STANDARD`.

Finish `of(...)` in `src/main/java/practice/AttributeKey.java`. When you're done, every call for
a standard name returns the same key object, wherever the name string came from. Any other name
still gets a key with that name, and nothing of it is kept afterwards, however many different
names the application sends. Keep the shared keys in `KEYS`, which `kept()`
reports. About two lines in total.
