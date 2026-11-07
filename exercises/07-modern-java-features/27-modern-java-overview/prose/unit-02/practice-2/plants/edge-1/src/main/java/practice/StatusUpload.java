package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads the shipment status files that branch offices upload, one status per line. The offices
 * write them with whatever editor or export tool they have.
 */
public class StatusUpload {

    static final Set<String> KNOWN = Set.of("RECEIVED", "PACKED", "SHIPPED", "DELIVERED", "RETURNED");

    /** The statuses in the upload, in file order. An unknown status rejects the whole upload. */
    public List<String> statuses(String upload) {
        List<String> statuses = new ArrayList<>();
        int number = 0;
        for (String line : lines(upload)) {
            number++;
            if (carriesNoStatus(line)) continue;
            if (!KNOWN.contains(line)) {
                throw new IllegalArgumentException("line " + number + ": unknown status '" + line + "'");
            }
            statuses.add(line);
        }
        return statuses;
    }

    /** The lines of the upload, without their line endings. */
    List<String> lines(String upload) {
        return List.of(upload.split("\n"));
    }

    /** True for a line an office left without a status. */
    boolean carriesNoStatus(String line) {
        return line.isBlank();
    }
}
