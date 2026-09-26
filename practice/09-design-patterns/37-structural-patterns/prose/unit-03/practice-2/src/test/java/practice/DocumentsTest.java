package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentsTest {

    /** The real service: a map of documents that records every call it receives. */
    static final class RecordingService implements Documents.DocumentService {
        final Map<String, String> docs = new HashMap<>(Map.of("doc1", "Public document content"));
        final List<String> calls = new ArrayList<>();

        @Override
        public String read(String docId) {
            calls.add("read " + docId);
            String content = docs.get(docId);
            if (content == null) {
                throw new NoSuchElementException("Document not found: " + docId);
            }
            return content;
        }

        @Override
        public String write(String docId, String content) {
            calls.add("write " + docId);
            docs.put(docId, content);
            return "Written to " + docId;
        }

        @Override
        public String delete(String docId) {
            calls.add("delete " + docId);
            docs.remove(docId);
            return "Deleted " + docId;
        }
    }

    private static Documents.DocumentService as(RecordingService real, String name, Documents.Role role) {
        return new Documents.ProtectionDocumentProxy(real, new Documents.User(name, role));
    }

    @Test
    void eachRoleGetsItsOperations() {
        RecordingService real = new RecordingService();

        assertThat(as(real, "guest", Documents.Role.GUEST).read("doc1")).isEqualTo("Public document content");
        assertThatThrownBy(() -> as(real, "guest", Documents.Role.GUEST).write("doc1", "x"))
                .isInstanceOf(SecurityException.class);
        assertThat(as(real, "bob", Documents.Role.USER).write("doc2", "draft")).isEqualTo("Written to doc2");
        assertThat(as(real, "root", Documents.Role.ADMIN).delete("doc2")).isEqualTo("Deleted doc2");
        assertThat(real.docs).containsOnlyKeys("doc1");
        assertThatThrownBy(() -> new Documents.ProtectionDocumentProxy(real, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Documents.ProtectionDocumentProxy(null, new Documents.User("bob", Documents.Role.USER)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aRefusedCallNeverReachesTheService() {
        RecordingService real = new RecordingService();

        assertThatThrownBy(() -> as(real, "guest", Documents.Role.GUEST).write("doc1", "defaced"))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> as(real, "bob", Documents.Role.USER).delete("doc1"))
                .isInstanceOf(SecurityException.class);

        assertThat(real.calls).isEmpty();
        assertThat(real.docs).containsEntry("doc1", "Public document content");
    }

    @Test
    void onlyAnAdminDeletes() {
        RecordingService real = new RecordingService();

        assertThatThrownBy(() -> as(real, "bob", Documents.Role.USER).delete("doc1"))
                .isInstanceOf(SecurityException.class);
        assertThat(real.docs).containsKey("doc1");
    }

    @Test
    void aMissingDocumentStaysAMissingDocument() {
        RecordingService real = new RecordingService();

        assertThatThrownBy(() -> as(real, "root", Documents.Role.ADMIN).read("nope"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Document not found: nope");
    }
}
