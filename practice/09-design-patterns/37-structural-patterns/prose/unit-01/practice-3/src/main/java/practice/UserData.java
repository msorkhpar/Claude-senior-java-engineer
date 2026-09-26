package practice;

import java.util.List;

public final class UserData {

    private UserData() {
    }

    /** The old system's user: data[0] = first name, data[1] = last name, data[2] = email. */
    public record LegacyUser(String[] data) {
    }

    /** The user the new system expects. */
    public record ModernUser(String fullName, String email) {
    }

    public static final class UserDataAdapter {

        public ModernUser adapt(LegacyUser legacyUser) {
            throw new UnsupportedOperationException("write adapt");
        }

        public List<ModernUser> adaptAll(List<LegacyUser> legacyUsers) {
            throw new UnsupportedOperationException("write adaptAll");
        }
    }
}
