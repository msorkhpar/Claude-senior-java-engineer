Operators unlock the deployment vault by typing a passphrase at a console, which hands it over as
a `char[]`, sometimes with a trailing `'\n'`. After an incident, the security team took a heap
dump of the deploy tool and found the passphrase in it twice, long after the unlock had finished.

The vault's client library takes the passphrase without the newline, reads it during `open`, and
keeps no reference to it. Write `unlock(typed)` in `src/main/java/practice/VaultUnlock.java`: it
opens the vault and returns the session id, or lets the vault's `IllegalArgumentException` for a
wrong passphrase reach the caller. `passphraseLength(typed)` is there to help.

When `unlock` has returned or thrown, every `char[]` that held the passphrase, the operator's
included, holds only `'\0'`. About seven lines in total.
