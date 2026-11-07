The baking blog's tag cloud shows `crème` twice, with a different count under each, and a reader
who searches for `jalapeño` from a tablet finds no posts although the cloud shows the tag. Both
come from authors and readers on different devices. A food blog also has tags that really are
different and only look alike, such as `rose` and `rosé`; those must stay apart.

`TagIndex` stores each tag under `key(tag)` and counts its posts. Finish the two `TODO`s in
`src/main/java/practice/TagIndex.java`:

- `key(...)`: the form a tag is stored and looked up in;
- `postsTagged(...)`: the number of posts for a reader's search.

About two lines in total. The next practice builds a tag value class on the same idea.
