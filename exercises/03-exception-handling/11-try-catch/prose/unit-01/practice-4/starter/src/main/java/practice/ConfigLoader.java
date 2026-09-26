package practice;

import java.io.IOException;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    /** Reads a named configuration file. */
    public interface Source {
        String read(String name) throws IOException;
    }

    /** Returns what {@code source} reads for {@code name}; a read failure becomes a ConfigException. */
    public static String load(Source source, String name) throws ConfigException {
        throw new UnsupportedOperationException("write load");
    }
}

/** A configuration file could not be processed. */
class ConfigException extends Exception {

    ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
