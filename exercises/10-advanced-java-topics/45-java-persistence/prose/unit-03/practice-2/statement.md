In the shop's back office, editors tag products by hand while a nightly job adds seasonal tags.
Nobody holds a lock while editing: an editor may sit on a product page for minutes. Since the job
started running during office hours, editors report tags that "didn't stick": they saved, saw the
tag, and an hour later it was gone. Every product row already has a `version` column that nothing
uses yet.

Finish the two `TODO`s in `src/main/java/practice/ProductTags.java`:

- `addTag(...)`: add one tag and save it, trying at most `ATTEMPTS` times;
- `save(...)`: write the new tags, unless someone else saved the product since it was read.

Read products with `load(...)`. The other writers keep the `version` column the way your `save`
does. When you're done, no writer's tag is lost, whoever saves first. About fifteen lines in total.

The tests run each editor on its own connection to an in-memory H2 database, and use the `Pause`
to let another writer save between an editor's read and its save.
