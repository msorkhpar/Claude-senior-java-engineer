package practice;

import java.util.Objects;

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

        private final DocumentService real;
        private final User user;

        public ProtectionDocumentProxy(DocumentService real, User user) {
            this.real = Objects.requireNonNull(real, "real service must not be null");
            this.user = Objects.requireNonNull(user, "user must not be null");
        }

        @Override
        public String read(String docId) {
            try {
                return real.read(docId);
            } catch (RuntimeException e) {
                throw new SecurityException(user.name() + " may not read " + docId);
            }
        }

        @Override
        public String write(String docId, String content) {
            if (user.role() == Role.GUEST) {
                throw new SecurityException(user.name() + " may not write");
            }
            return real.write(docId, content);
        }

        @Override
        public String delete(String docId) {
            if (user.role() != Role.ADMIN) {
                throw new SecurityException(user.name() + " may not delete");
            }
            return real.delete(docId);
        }
    }
}
