package practice;

public final class Server {

    /** The server's settings: a timeout and a host. */
    public static final class Config {

        private int timeout;
        private String host;

        public Config(int timeout, String host) {
            this.timeout = timeout;
            this.host = host;
        }

        public int timeout() {
            return timeout;
        }

        public String host() {
            return host;
        }
    }

    private volatile Config config;       // the one publication point

    public Server(int timeout, String host) {
        this.config = new Config(timeout, host);
    }

    /** Returns the current settings. */
    public Config config() {
        return config;
    }

    /** Changes the timeout, keeping the host. */
    public void withTimeout(int timeout) {
        config.timeout = timeout;                         // edit in place
    }

    /** Changes the host, keeping the timeout. */
    public void withHost(String host) {
        config.host = host;
    }
}
