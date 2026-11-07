Members of a community site choose a public handle at sign-up. Last week someone registered
`ｓｕｐｐｏｒｔ`, in full-width letters. On a profile page it looks like the staff account, and the
mention service, which looks handles up after NFKC normalisation, now delivers some `@support`
mentions to it. A second account, `ѕupport`, starts with a Cyrillic letter.

`HandleRegistry.register` already refuses reserved and taken handles. Finish the two `TODO`s in
`src/main/java/practice/HandleRegistry.java`:

- `canonical(raw)`: the form of the handle that the registry checks, stores and compares;
- `allowed(handle)`: whether the handle keeps to the rules below.

A handle is 3 to 20 characters long, each one a lowercase letter of the Latin script (accents are
fine: `zoë_m`, `renée`), a digit `0`–`9`, or `_`. When you're done, any two spellings the mention
service treats as one handle are one handle here too, and a handle that only looks like a Latin
one is refused. About three lines in total.
