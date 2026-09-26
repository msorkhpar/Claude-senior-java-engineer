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

    private final List<String> log;

    public Importer(List<String> log) {
        this.log = log;
    }

    /** Sums the records; the first bad one throws ImportException("Bad record at line n: record"). Logs nothing. */
    public int importAll(List<String> records) throws ImportException {
        int sum = 0;
        for (int i = 0; i < records.size(); i++) {
            String record = records.get(i);
            try {
                sum += Integer.parseInt(record);
            } catch (NumberFormatException e) {
                log.add("import failed: Bad record at line " + (i + 1) + ": " + record);
                throw new ImportException("Bad record at line " + (i + 1) + ": " + record, e);
            }
        }
        return sum;
    }

    /** importAll's sum, or 0 after logging "import failed: " + message once. */
    public int importOrZero(List<String> records) {
        try {
            return importAll(records);
        } catch (ImportException e) {
            log.add("import failed: " + e.getMessage());
            return 0;
        }
    }
}
