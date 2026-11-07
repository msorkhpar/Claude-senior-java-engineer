The customers table keeps each customer's tax id encrypted, one byte array per row in a single
column. Several instances of the service share one AES key from the key store, and the nightly
export job reads the column with the same key. Support staff can edit rows directly in the
database.

Finish `seal` and `open` in `src/main/java/practice/ColumnCipher.java`. When you're done:

- whatever `seal` returns is all that is stored, and any `ColumnCipher` with the same key opens
  it back into the same tax id;
- the same tax id sealed twice is never stored with the same IV;
- a stored value that was changed in the database is refused with a `GeneralSecurityException`
  (or a subclass), never returned as a tax id.

The tests use their own key, generated when they start, and a `new SecureRandom()`. About fifteen
lines in total.
