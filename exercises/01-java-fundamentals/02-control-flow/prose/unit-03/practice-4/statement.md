The desktop client shows "files up to N bytes" next to its upload button. Customers put different
storage gateways in front of the service, each with its own size limit, so the client finds N by
probing: each probe sends a request of one size to the gateway's test endpoint, which accepts it
or refuses it. A gateway that accepts a size accepts every smaller one. Probes are slow, so the
client can afford a few dozen of them, and some gateways are configured with a ceiling of
`Integer.MAX_VALUE` bytes.

Write `largestAccepted(maxBytes, accepts)` in `src/main/java/practice/UploadLimit.java`: the
largest size in `1..maxBytes` that the gateway accepts, or `0` when it refuses even one byte. Probe
only sizes in that range. About eight lines.

This is the mirror of a search for the first bad build: here the answer is the last size that
passes.
