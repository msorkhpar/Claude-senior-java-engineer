package practice;

import java.util.List;

record Entry(String id, boolean valid) {
}

record Report(List<String> validIds, long validCount, long seen) {
}

public final class Audit {

    private Audit() {
    }

    public static Report audit(List<Entry> entries) {
        throw new UnsupportedOperationException("write audit");
    }
}
