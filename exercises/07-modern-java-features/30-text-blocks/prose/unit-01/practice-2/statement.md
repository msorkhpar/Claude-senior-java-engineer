The health checker probes each service over a plain socket and writes its HTTP/1.1 request by
hand. On a developer's Windows laptop the probes pass. On the Linux monitoring host the load
balancer answers every one of them with `400 Bad Request`, and the service behind it never sees
the request.

`HealthProbe.send` already writes the request head to the socket. Build that head in the `TODO`
of `src/main/java/practice/HealthProbe.java`: `head(host, path, accept)` returns

```text
GET <path> HTTP/1.1
Host: <host>
Accept: <accept>

```

where every line, the empty last one included, ends with a carriage return and a line feed, as
HTTP/1.1 requires.

When you're done, the probe sends the same bytes from Linux and from Windows, and a strict
server reads three lines and an empty one. About four lines in total.
