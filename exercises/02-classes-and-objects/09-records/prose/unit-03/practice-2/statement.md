Records suit **configuration objects**: a configuration is a small, fixed set of
values that should not change once made. A record cannot be changed after
creation, so "changing" one means making a copy with one component replaced, a
method often called a *wither*.

Complete `record ServerConfig(String host, int port, int timeoutSeconds)`:

- **compact constructor**: a `null` or blank host, a port outside `1..65535`,
  and a timeout of 0 seconds or less are refused with an
  `IllegalArgumentException`;
- **`static ServerConfig defaults()`**: `localhost`, port `8080`, `30` seconds;
- **`ServerConfig withPort(int port)`** and
  **`ServerConfig withTimeoutSeconds(int seconds)`**: a new configuration with
  that one component replaced and every other component kept.

Every configuration, however it is made, must be valid.

## Examples

```
ServerConfig.defaults()                         -> ServerConfig[host=localhost, port=8080, timeoutSeconds=30]
ServerConfig.defaults().withPort(9090)          -> ServerConfig[host=localhost, port=9090, timeoutSeconds=30]
new ServerConfig("api.example.com", 443, 5).withPort(8443)
                                                -> ServerConfig[host=api.example.com, port=8443, timeoutSeconds=5]
new ServerConfig("localhost", 0, 30)            -> IllegalArgumentException
ServerConfig.defaults().withPort(70000)         -> IllegalArgumentException
```
