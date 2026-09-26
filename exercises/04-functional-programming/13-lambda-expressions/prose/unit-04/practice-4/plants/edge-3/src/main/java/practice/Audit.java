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
        List<String> validIds = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        entries.parallelStream()
                .filter(Entry::valid)
                .map(Entry::id)
                .forEach(validIds::add);
        return new Report(validIds, validIds.size(), entries.size());
    }
}
