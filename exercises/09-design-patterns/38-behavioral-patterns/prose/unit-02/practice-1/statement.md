The page's analogy is a newspaper: the publisher (the **subject**) keeps a
list of subscribers (the **observers**) and delivers each new edition to
everyone on it. Subscribers join and leave at any time.

`Observer<T>` (given) has `void update(String event, T data)`. Write
`NewsAgency`:

- `addObserver(o)` registers `o`; `null` throws `IllegalArgumentException`.
  **An observer already registered** (by `equals`) **is not added again**.
- `removeObserver(o)` unregisters `o`. **Removing an observer that is not
  registered does nothing** (no exception, also for `null`).
- `publishArticle(article)` records the article, then calls
  `update("NEW_ARTICLE", article)` on every observer. **A blank article**
  (`null`, empty or only spaces) **is refused** with
  `IllegalArgumentException` before anyone is notified.
- `getPublishedArticles()` returns the articles in publishing order; **the
  list a caller gets can never change the agency's record**.
- `observerCount()` returns how many observers are registered.

| calls | observers get | `getPublishedArticles()` |
|---|---|---|
| add A, add B, publish `"Java 21"` | A and B get `"Java 21"` | `["Java 21"]` |
| add A, add A, publish `"x"` | A gets `"x"` once | `["x"]` |
| add A, remove A, publish `"y"` | nothing | `["y"]` |
| publish `"   "` | nothing | `[]`, and `IllegalArgumentException` |
