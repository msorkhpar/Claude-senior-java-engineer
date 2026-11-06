This practice continues the previous one: `TlsCredentials` now keeps the certificate and its key
together, and a reload replaces them safely. The listener also needs the server's side of a
handshake, which sends the certificate to the client and then signs with the private key; the
client checks the signature against the certificate it received.

Finish `serve(...)` in `src/main/java/practice/TlsCredentials.java`. Reloads keep happening on the
reload thread while handshakes run, and a reload can complete between the two steps of a
handshake. When you're done, every handshake signs with the key of the certificate it sent, and
the handshakes after a reload use the renewed pair. About three lines in total.

The tests complete a reload on another thread right after the certificate is sent.
