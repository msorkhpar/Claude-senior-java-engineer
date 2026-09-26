package practice;

public record ServerConfig(String host, int port, int timeoutSeconds) {

    /** Refuse a null or blank host, a port outside 1..65535 and a timeout of 0 or less. */
    public ServerConfig {
        throw new UnsupportedOperationException("write the compact constructor");
    }

    /** localhost, port 8080, 30 seconds. */
    public static ServerConfig defaults() {
        throw new UnsupportedOperationException("write defaults");
    }

    /** A new configuration with this port and every other component kept. */
    public ServerConfig withPort(int port) {
        throw new UnsupportedOperationException("write withPort");
    }

    /** A new configuration with this timeout and every other component kept. */
    public ServerConfig withTimeoutSeconds(int seconds) {
        throw new UnsupportedOperationException("write withTimeoutSeconds");
    }
}
