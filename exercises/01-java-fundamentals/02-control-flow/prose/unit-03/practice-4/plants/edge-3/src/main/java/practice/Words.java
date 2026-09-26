package practice;

import java.io.BufferedReader;
import java.io.IOException;

public final class Words {

    private Words() {
    }

    /** Returns the number of words in the whole input. */
    public static int count(BufferedReader reader) throws IOException {
        int count = 0;
        String line = reader.readLine();
        do {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                count += trimmed.split("\\s+").length;
            }
        } while ((line = reader.readLine()) != null);
        return count;
    }
}
