The sign-up form saves names and addresses that users type, or paste from web pages, word
processors and spreadsheets. The duplicate-customer report keeps finding pairs such as `Tomás`
and a `Tomás` that looks identical, and support found accounts whose required last name is
blank on screen but passed validation.

`SignupForm.accept(...)` already cleans every field and checks the required ones. Finish the two
`TODO`s in `src/main/java/practice/SignupForm.java`:

- `clean(value)`: the value without the blank-looking characters around it;
- `missing(value)`: whether the value shows as nothing at all.

When you're done, a pasted value is saved the same as the typed one, and a required field that
shows nothing is reported missing. About three lines in total.
