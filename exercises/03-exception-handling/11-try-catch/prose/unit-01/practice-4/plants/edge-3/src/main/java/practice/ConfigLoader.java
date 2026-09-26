package practice;

import java.io.IOException;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public interface Source {
        String read(String name) throws IOException;
    }

    public static String load(Source source, String name) throws ConfigException {
        try {
            return source.read(name);
        } catch (IOException first) {
            try {
                return source.read(name);
            } catch (IOException e) {
                throw new ConfigException("Error processing file " + name, e);
            }
        }
    }
}

class ConfigException extends Exception {

    ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
