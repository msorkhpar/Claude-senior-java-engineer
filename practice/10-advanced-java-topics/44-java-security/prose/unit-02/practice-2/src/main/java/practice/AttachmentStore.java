package practice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Serves ticket attachments: GET /attachments?name=... reads one file of the store. */
public class AttachmentStore {

    private final Path base;

    /** {@code base} is the store's directory, as configured; it may be reached through a link. */
    public AttachmentStore(Path base) {
        this.base = base;
    }

    /** The text of the attachment the request names. */
    public String read(String name) throws IOException {
        return Files.readString(locate(name));
    }

    /**
     * The file a request for {@code name} may read. A name that leads to a file outside the
     * store's directory is refused with a SecurityException.
     */
    Path locate(String name) throws IOException {
        // TODO: name comes straight from the request. Return the file to read, and refuse any
        //       name that would make the store hand out a file that isn't one of its own.
        throw new IllegalStateException("TODO: locate");
    }
}
