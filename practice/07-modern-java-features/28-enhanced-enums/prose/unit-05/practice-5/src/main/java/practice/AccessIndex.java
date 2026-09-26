package practice;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class AccessIndex {

    private AccessIndex() {
    }

    public enum Permission { READ, WRITE, EXECUTE, DELETE, ADMIN }

    /** For every permission, the users holding it, sorted by name, in declaration order of the permissions. */
    public static Map<Permission, List<String>> whoHas(Map<String, Set<Permission>> grants) {
        throw new UnsupportedOperationException("write whoHas");
    }
}
