package practice;

public final class Server {

    /** The server's settings: a timeout and a host. */
    public static final class Config {

        public Config(int timeout, String host) {
            throw new UnsupportedOperationException("write Config");
        }

        public int timeout() {
            throw new UnsupportedOperationException("write timeout");
        }

        public String host() {
            throw new UnsupportedOperationException("write host");
        }
    }

    public Server(int timeout, String host) {
        throw new UnsupportedOperationException("write Server");
    }

    /** Returns the current settings. */
    public Config config() {
        throw new UnsupportedOperationException("write config");
    }

    /** Changes the timeout, keeping the host. */
    public void withTimeout(int timeout) {
        throw new UnsupportedOperationException("write withTimeout");
    }

    /** Changes the host, keeping the timeout. */
    public void withHost(String host) {
        throw new UnsupportedOperationException("write withHost");
    }
}
