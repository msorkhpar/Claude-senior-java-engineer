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
        long[] count = {0};
        List<String> validIds = entries.stream()
                .filter(entry -> {
                    count[0]++;
                    return entry.valid();
                })
                .map(Entry::id)
                .toList();
        return new Report(validIds, count[0], entries.size());
    }
}
