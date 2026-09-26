package practice;

import java.util.List;

public final class FieldAudit {

    private FieldAudit() {
    }

    /** The sorted names of the fields {@code type} declares that are neither private nor constants. */
    public static List<String> exposed(Class<?> type) {
        throw new UnsupportedOperationException("write exposed");
    }
}
