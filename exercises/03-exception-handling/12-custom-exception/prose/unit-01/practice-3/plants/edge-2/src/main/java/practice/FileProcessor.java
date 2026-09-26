package practice;

import java.io.IOException;

/** Reads a file's whole content by name. */
interface FileStore {
    String read(String name) throws IOException;
}

/** Checked: a file could not be processed. Carries the file's name. */
class FileProcessingException extends Exception {

    private String fileName;

    FileProcessingException(String message, String fileName, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }

    String getFileName() {
        return fileName;
    }
}

public class FileProcessor {

    private final FileStore store;

    public FileProcessor(FileStore store) {
        this.store = store;
    }

    /** Counts the lines of the named file; a failed read becomes a FileProcessingException. */
    public int countLines(String fileName) throws FileProcessingException {
        try {
            return (int) store.read(fileName).lines().count();
        } catch (IOException e) {
            throw new FileProcessingException("Error processing file: " + fileName, fileName, e);
        }
    }
}
