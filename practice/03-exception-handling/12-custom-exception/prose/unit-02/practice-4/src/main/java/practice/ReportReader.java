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

    public ReportReader(ReportSource source) {
    }

    /** Returns the report; a failed fetch becomes ReportException("Could not read report " + name). */
    public String read(String name) throws ReportException {
        throw new UnsupportedOperationException("write read");
    }
}
