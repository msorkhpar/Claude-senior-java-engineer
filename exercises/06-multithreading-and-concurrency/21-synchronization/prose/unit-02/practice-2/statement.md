The API gateway terminates TLS. When its certificate is renewed, a reload thread reads the new
certificate and private key from the secrets vault and the listener starts presenting them,
without a restart. Since the last renewal, monitoring has shown a short burst of failed
handshakes at every reload: clients received a certificate that the server's signature didn't
match. Both fields in `TlsCredentials` are already `volatile`.

Finish the two `TODO`s in `src/main/java/practice/TlsCredentials.java`:

- `reload(...)`: read the renewed certificate and key from the source and start using them;
- `current()`: the certificate and key a new handshake uses.

When you're done, every handshake gets a certificate with the key that belongs to it, whenever it
starts, and a handshake never waits for a reload, however slow the vault is. You may change how
the credentials are stored. About five lines in total.

The tests hold the reload while it reads one part from the vault and start a handshake meanwhile.
