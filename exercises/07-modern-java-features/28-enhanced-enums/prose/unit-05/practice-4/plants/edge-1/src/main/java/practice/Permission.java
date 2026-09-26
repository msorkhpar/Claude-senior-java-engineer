package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum Permission {
    READ, WRITE, EXECUTE, DELETE, ADMIN;

    public static final Set<Permission> READ_ONLY = Collections.unmodifiableSet(EnumSet.of(READ));
    public static final Set<Permission> READ_WRITE = Collections.unmodifiableSet(EnumSet.of(READ, WRITE));
    public static final Set<Permission> FULL_ACCESS = Collections.unmodifiableSet(EnumSet.allOf(Permission.class));
    public static final Set<Permission> NO_ACCESS = Collections.unmodifiableSet(EnumSet.noneOf(Permission.class));

    /** The union of {@code sets}; empty when there are none. */
    @SafeVarargs
    public static Set<Permission> combine(Set<Permission>... sets) {
        List<Permission> all = new ArrayList<>();
        for (Set<Permission> set : sets) {
            all.addAll(set);
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(all));
    }

    /** The permissions every one of {@code sets} holds; empty when there are none. */
    @SafeVarargs
    public static Set<Permission> intersect(Set<Permission>... sets) {
        if (sets.length == 0) {
            return Collections.unmodifiableSet(EnumSet.noneOf(Permission.class));
        }
        EnumSet<Permission> common = EnumSet.allOf(Permission.class);
        for (Set<Permission> set : sets) {
            common.retainAll(set);
        }
        return Collections.unmodifiableSet(common);
    }
}
