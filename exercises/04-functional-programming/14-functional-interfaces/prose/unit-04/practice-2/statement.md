The admin console shows each settings file with its description on top: the block of comment
lines a file opens with. Below it, the console lists the rest of the file, which it rewrites line
for line when someone changes a value. Operators disable a setting by putting `#` in front of it,
and since then the console shows `# smtp.debug=true` as part of a file's description, and a save
drops the disabled settings from the file.

Write `description(...)` and `settings(...)` in `src/main/java/practice/SettingsFile.java`, using
`comment(line)` to recognise a comment. The description is the comment block at the top of the
file; the settings section is every line after it, in file order. About two lines in total.
