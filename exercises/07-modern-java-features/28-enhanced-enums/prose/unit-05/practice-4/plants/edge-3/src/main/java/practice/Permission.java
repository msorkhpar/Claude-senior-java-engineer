package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum Permission {
    READ, WRITE, EXECUTE, DELETE, ADMIN;

    public static final Set<Permission> READ_ONLY = EnumSet.of(READ);
    public static final Set<Permission> READ_WRITE = EnumSet.of(READ, WRITE);
    public static final Set<Permission> FULL_ACCESS = EnumSet.allOf(Permission.class);
    public static final Set<Permission> NO_ACCESS = EnumSet.noneOf(Permission.class);

    /** The union of {@code sets}; empty when there are none. */
    @SafeVarargs
    public static Set<Permission> combine(Set<Permission>... sets) {
        EnumSet<Permission> union = EnumSet.noneOf(Permission.class);
        for (Set<Permission> set : sets) {
            union.addAll(set);
        }
        return Collections.unmodifiableSet(union);
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
