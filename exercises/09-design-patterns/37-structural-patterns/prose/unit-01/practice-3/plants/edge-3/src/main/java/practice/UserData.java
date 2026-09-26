package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            Objects.requireNonNull(legacyUser, "legacy user must not be null");
            String[] data = legacyUser.data();
            if (data.length < 3) {
                throw new IllegalArgumentException("a legacy user needs at least 3 fields, got " + data.length);
            }
            return new ModernUser(data[0] + " " + data[1], data[2]);
        }

        public List<ModernUser> adaptAll(List<LegacyUser> legacyUsers) {
            if (legacyUsers.isEmpty()) {
                throw new IllegalArgumentException("nothing to adapt");
            }
            List<ModernUser> result = new ArrayList<>();
            for (LegacyUser user : legacyUsers) {
                result.add(adapt(user));
            }
            return result;
        }
    }
}
