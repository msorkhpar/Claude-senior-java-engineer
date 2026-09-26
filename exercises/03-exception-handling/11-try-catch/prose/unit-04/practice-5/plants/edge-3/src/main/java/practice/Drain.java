package practice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Drain {

    private Drain() {
    }

    public interface LineSource extends AutoCloseable {
        String readLine() throws IOException;

        @Override
        void close() throws IOException;
    }

    public static List<String> readAll(LineSource source) throws IOException {
        List<String> lines = new ArrayList<>();
        try {
            String line;
            while ((line = source.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            source.close();
        }
        return lines;
    }
}
