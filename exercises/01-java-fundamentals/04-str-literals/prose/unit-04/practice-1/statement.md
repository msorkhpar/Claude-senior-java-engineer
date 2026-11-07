Every time a customer presses Save in the profile editor, the audit log lists the fields the save
changed, and the customer gets a "your profile was updated" mail naming them. Since the editor
was rewritten, the log lists fields that nobody touched, and saving a profile with its website
cleared fails with an exception instead of being logged.

The stored values come from the database as `String`s. The editor hands over each submitted value
as whatever `CharSequence` it keeps the text in. An empty field is `null`, on either side.

`ProfileAudit.changedFields(...)` already walks the fields. Finish the `TODO` in `changed(...)` in
`src/main/java/practice/ProfileAudit.java`: a field changed when its text is different, when it
was empty and now has text, or when it had text and is now empty. About four lines in total.
