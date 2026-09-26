package practice;

public record ServerConfig(String host, int port, int timeoutSeconds) {

    /** Refuse a null or blank host, a port outside 1..65535 and a timeout of 0 or less. */
    public ServerConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("A host is required");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
    }

    /** localhost, port 8080, 30 seconds. */
    public static ServerConfig defaults() {
        return new ServerConfig("localhost", 8080, 30);
    }

    /** A new configuration with this port and every other component kept. */
    public ServerConfig withPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port out of range: " + port);
        }
        return new ServerConfig(host, port, timeoutSeconds);
    }

    /** A new configuration with this timeout and every other component kept. */
    public ServerConfig withTimeoutSeconds(int seconds) {
        return new ServerConfig(host, port, seconds);
    }
}
