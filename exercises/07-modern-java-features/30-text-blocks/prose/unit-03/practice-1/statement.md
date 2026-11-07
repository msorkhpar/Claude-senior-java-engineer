Twice this quarter a text block passed review and then produced the wrong string in production:
once a padded record that came out short, once a message whose every line had gained four
spaces. A diff can't show either. The team's review bot will now post, under every text block a
pull request changes, the exact string the compiler makes of it.

`TextBlockPreview.value(...)` in `src/main/java/practice/TextBlockPreview.java` receives the
block's source lines exactly as they are in the file: everything after the opening delimiter's
line, up to and including the line with the closing `"""`. It already splits off the closing
delimiter and joins the lines. Finish the two `TODO`s:

- `margin(...)`: how many leading characters javac removes from every line of the block;
- `strip(...)`: what is left of one source line in the value.

When you're done, `value(...)` returns exactly the string javac produces for any block of spaces,
tabs and text, without escape sequences. About eight lines in total.

The tests compare with values that javac 21 produced from the same lines.
