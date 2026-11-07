package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttachmentStoreTest {

    @TempDir
    Path tmp;

    private Path root;

    /*
     * root/
     *   attachments/            the store
     *     T-118/screenshot.txt
     *     T-204/log.txt
     *     archive -> ../exports (a link someone made)
     *   attachments-old/notes.txt
     *   exports/ledger.csv
     *   service.properties
     *   current -> attachments  (how the service is configured on some hosts)
     */
    @BeforeEach
    void layOutTheDisk() throws IOException {
        root = tmp.toRealPath();
        Path store = Files.createDirectories(root.resolve("attachments"));
        Files.createDirectories(store.resolve("T-118"));
        Files.createDirectories(store.resolve("T-204"));
        Files.writeString(store.resolve("T-118/screenshot.txt"), "screenshot of T-118");
        Files.writeString(store.resolve("T-204/log.txt"), "log of T-204");
        Files.createDirectories(root.resolve("attachments-old"));
        Files.writeString(root.resolve("attachments-old/notes.txt"), "old notes");
        Files.createDirectories(root.resolve("exports"));
        Files.writeString(root.resolve("exports/ledger.csv"), "ledger");
        Files.writeString(root.resolve("service.properties"), "db.password=placeholder");
        Files.createSymbolicLink(store.resolve("archive"), Path.of("../exports"));
        Files.createSymbolicLink(root.resolve("current"), Path.of("attachments"));
    }

    @Test
    void readsTheStoresOwnFiles() throws IOException {
        AttachmentStore store = new AttachmentStore(root.resolve("attachments"));

        assertThat(store.read("T-118/screenshot.txt")).isEqualTo("screenshot of T-118");
        assertThat(store.read("T-204/log.txt")).isEqualTo("log of T-204");
        assertThat(store.read("T-204/../T-118/screenshot.txt")).isEqualTo("screenshot of T-118");
    }

    @Test
    void aNameThatClimbsOutOfTheStoreIsRefused() {
        AttachmentStore store = new AttachmentStore(root.resolve("attachments"));

        assertThatThrownBy(() -> store.read("T-118/../../service.properties"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void aDirectoryWhoseNameStartsLikeTheStoreIsStillOutside() {
        AttachmentStore store = new AttachmentStore(root.resolve("attachments"));

        assertThatThrownBy(() -> store.read("../attachments-old/notes.txt"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void aLinkInsideTheStoreDoesNotLeadOutOfIt() {
        AttachmentStore store = new AttachmentStore(root.resolve("attachments"));

        assertThatThrownBy(() -> store.read("archive/ledger.csv"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void aStoreConfiguredThroughALinkStillServesItsFiles() throws IOException {
        AttachmentStore store = new AttachmentStore(root.resolve("current"));

        assertThat(store.read("T-204/log.txt")).isEqualTo("log of T-204");
    }
}
