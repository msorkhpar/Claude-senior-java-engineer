package practice;

import java.io.BufferedReader;
import java.io.IOException;

public final class Words {

    private Words() {
    }

    /** Returns the number of words in the whole input. */
    public static int count(BufferedReader reader) throws IOException {
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            count += line.trim().split("\\s+").length;
        }
        return count;
    }
}
