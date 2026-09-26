package practice;

import java.io.IOException;

/** Fetches a report's text by name. */
interface ReportSource {
    String fetch(String name) throws IOException;
}

/** Checked: a report could not be read. */
class ReportException extends Exception {
    ReportException(String message) {
        super(message);
    }

    ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class ReportReader {

    private final ReportSource source;

    public ReportReader(ReportSource source) {
        this.source = source;
    }

    /** Returns the report; a failed fetch becomes ReportException("Could not read report " + name). */
    public String read(String name) throws ReportException {
        try {
            return source.fetch(name);
        } catch (IOException e) {
            throw new ReportException("Could not read report " + name, e);
        }
    }
}
