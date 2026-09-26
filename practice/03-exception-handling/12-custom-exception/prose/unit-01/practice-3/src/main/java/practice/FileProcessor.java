package practice;

import java.io.IOException;

/** Reads a file's whole content by name. */
interface FileStore {
    String read(String name) throws IOException;
}

/** Checked: a file could not be processed. Carries the file's name. */
class FileProcessingException extends Exception {

    FileProcessingException(String message, String fileName, Throwable cause) {
        throw new UnsupportedOperationException("write FileProcessingException(String, String, Throwable)");
    }

    String getFileName() {
        throw new UnsupportedOperationException("write getFileName");
    }
}

public class FileProcessor {

    public FileProcessor(FileStore store) {
    }

    /** Counts the lines of the named file; a failed read becomes a FileProcessingException. */
    public int countLines(String fileName) throws FileProcessingException {
        throw new UnsupportedOperationException("write countLines");
    }
}
