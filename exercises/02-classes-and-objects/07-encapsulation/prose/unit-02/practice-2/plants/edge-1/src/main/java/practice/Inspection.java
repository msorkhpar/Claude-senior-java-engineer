package practice;

import java.util.Date;
import java.util.List;

/**
 * A filed safety inspection of a building site: when it took place and the defects the inspector
 * found. Once filed it is evidence, and nothing may change it.
 */
public final class Inspection {

    private final String site;
    private final Date inspectedAt;
    private final String[] defects;

    public Inspection(String site, Date inspectedAt, String... defects) {
        this.site = site;
        this.inspectedAt = new Date(inspectedAt.getTime());
        this.defects = defects.clone();
    }

    public String getSite() {
        return site;
    }

    /** When the inspection took place. */
    public Date getInspectedAt() {
        return inspectedAt;
    }

    /** The defects found, in the order the inspector wrote them down. */
    public List<String> getDefects() {
        return List.of(defects);
    }

    public int defectCount() {
        return defects.length;
    }
}
