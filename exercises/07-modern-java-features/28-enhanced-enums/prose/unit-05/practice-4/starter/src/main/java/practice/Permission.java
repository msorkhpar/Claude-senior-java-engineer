package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum Permission {
    READ, WRITE, EXECUTE, DELETE, ADMIN;

    // write the four shared sets: READ_ONLY, READ_WRITE, FULL_ACCESS, NO_ACCESS
    public static final Set<Permission> READ_ONLY = null;
    public static final Set<Permission> READ_WRITE = null;
    public static final Set<Permission> FULL_ACCESS = null;
    public static final Set<Permission> NO_ACCESS = null;

    /** The union of {@code sets}; empty when there are none. */
    @SafeVarargs
    public static Set<Permission> combine(Set<Permission>... sets) {
        throw new UnsupportedOperationException("write combine");
    }

    /** The permissions every one of {@code sets} holds; empty when there are none. */
    @SafeVarargs
    public static Set<Permission> intersect(Set<Permission>... sets) {
        throw new UnsupportedOperationException("write intersect");
    }
}
