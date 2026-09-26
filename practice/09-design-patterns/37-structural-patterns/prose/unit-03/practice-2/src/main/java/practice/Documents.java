package practice;

public final class Documents {

    private Documents() {
    }

    public interface DocumentService {
        String read(String docId);

        String write(String docId, String content);

        String delete(String docId);
    }

    public enum Role { ADMIN, USER, GUEST }

    public record User(String name, Role role) {
    }

    public static final class ProtectionDocumentProxy implements DocumentService {

        public ProtectionDocumentProxy(DocumentService real, User user) {
            throw new UnsupportedOperationException("write the constructor");
        }

        @Override
        public String read(String docId) {
            throw new UnsupportedOperationException("write read");
        }

        @Override
        public String write(String docId, String content) {
            throw new UnsupportedOperationException("write write");
        }

        @Override
        public String delete(String docId) {
            throw new UnsupportedOperationException("write delete");
        }
    }
}
