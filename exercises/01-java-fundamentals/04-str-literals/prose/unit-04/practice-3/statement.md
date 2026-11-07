This builds on the previous practice. The tag cloud now shows `Linux`, `LINUX` and `linux` as
three tags, and the blog's editors want one, shown the way it was first typed. So the index keeps
each tag's text in a `Tag` and uses `Tag`s as the keys of its map; `key(...)` from the previous
practice is already there.

Finish `equals` and `hashCode` of `TagIndex.Tag` in `src/main/java/practice/TagIndex.java`: two
tags are the same tag when they differ only in case, or in how a device sent their accented
letters. The blog runs on servers in several countries, with different default locales. About
four lines in total.
