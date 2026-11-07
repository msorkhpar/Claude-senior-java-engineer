package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Decides which fields of a customer profile a save changed, for the audit log and for the
 * "your profile was updated" mail. Stored values come from the database as Strings; submitted
 * values come from the editor as it holds them. Either side is null when the field is empty.
 */
public class ProfileAudit {

    /** The names of the submitted fields whose value differs from the stored one, in submission order. */
    public List<String> changedFields(Map<String, String> stored, Map<String, CharSequence> submitted) {
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, CharSequence> field : submitted.entrySet()) {
            if (changed(stored.get(field.getKey()), field.getValue())) {
                changed.add(field.getKey());
            }
        }
        return changed;
    }

    /** Whether a field whose stored value is {@code stored} now holds {@code submitted}. */
    static boolean changed(String stored, CharSequence submitted) {
        if (stored == null) {
            return submitted != null;
        }
        return !stored.contentEquals(submitted);
    }
}
