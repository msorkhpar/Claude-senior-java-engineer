package practice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public final class FirstLine {

    private FirstLine() {
    }

    /** Returns the first line of {@code source} and always closes it. */
    public static String read(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
}
