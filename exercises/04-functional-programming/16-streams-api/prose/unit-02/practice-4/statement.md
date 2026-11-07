The composer under a post's text box shows `used/limit`, and the preview cuts a post that is over
the limit. The product spec counts one character per Unicode code point, so 🎉 is one character.
Users report that a post with a few emoji shows a count higher than the characters they typed,
and that some previews end in a `?` box where an emoji was cut.

Finish the two `TODO` methods in `src/main/java/practice/PostLimit.java`:

- `length(post)`: the post's length by the spec's count;
- `truncate(post, max)`: the first `max` characters of the post, by the same count.

About four lines in total. The tests use emoji that are each one code point; flags and emoji with
skin tones, which are several, are out of scope.
