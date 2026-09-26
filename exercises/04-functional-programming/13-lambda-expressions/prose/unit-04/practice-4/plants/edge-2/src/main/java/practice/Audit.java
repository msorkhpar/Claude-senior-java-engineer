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
        long[] seen = {0};
        entries.stream().peek(entry -> seen[0]++).count();
        List<String> validIds = entries.stream()
                .filter(Entry::valid)
                .map(Entry::id)
                .toList();
        return new Report(validIds, validIds.size(), seen[0]);
    }
}
