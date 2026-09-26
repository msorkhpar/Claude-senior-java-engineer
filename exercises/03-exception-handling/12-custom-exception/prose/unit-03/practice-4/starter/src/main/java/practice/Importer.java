package practice;

import java.util.List;

/** Checked: a batch could not be imported. */
class ImportException extends Exception {
    ImportException(String message) {
        super(message);
    }

    ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class Importer {

    public Importer(List<String> log) {
    }

    /** Sums the records; the first bad one throws ImportException("Bad record at line n: record"). Logs nothing. */
    public int importAll(List<String> records) throws ImportException {
        throw new UnsupportedOperationException("write importAll");
    }

    /** importAll's sum, or 0 after logging "import failed: " + message once. */
    public int importOrZero(List<String> records) {
        throw new UnsupportedOperationException("write importOrZero");
    }
}
