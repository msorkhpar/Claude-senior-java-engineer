package practice;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The nightly import of electricity meter readings. Each file holds one reading per line, in
 * kilowatt-hours. A file that can't be read whole is skipped and retried the next night.
 */
public class MeterImport {

    static final Logger LOG = Logger.getLogger("practice.meters");

    /** A file of readings could not be imported. */
    public static class ImportException extends Exception {
        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Parses one line; throws NumberFormatException when it isn't a whole number of kWh. */
    static long parseReading(String line) {
        return Long.parseLong(line.strip());
    }

    /** Returns the total kWh of the file, or throws ImportException naming the file and the line. */
    long importFile(String file, List<String> lines) throws ImportException {
        // TODO: Add up the file's readings with parseReading. A line that isn't a reading makes
        //       the whole file fail with ImportException, saying which file and which line.
        throw new IllegalStateException("TODO: importFile");
    }

    /** The nightly job: imports one file and returns the line it prints to the job report. */
    public String runNightly(String file, List<String> lines) {
        // TODO: Return "imported <file>: <total> kWh". When the file can't be imported, return
        //       "skipped <file>"; whoever investigates tomorrow morning must find out from the
        //       log why it was skipped.
        throw new IllegalStateException("TODO: runNightly");
    }
}
