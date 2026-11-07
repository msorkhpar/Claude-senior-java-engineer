package practice;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * What each role may do on a shared document. A user has a role and may hold extra grants on
 * top of it; the extra grants are stored in the {@code extra_grants} text column of the
 * {@code memberships} table.
 */
public final class Grants {

    public enum Access { VIEW, COMMENT, EDIT, SHARE, DELETE }

    public enum Role { VIEWER, COMMENTER, EDITOR, MANAGER, OWNER }

    /** The base grants of each role. Every request reads them. */
    private static final Map<Role, Set<Access>> BASE = new EnumMap<>(Role.class);
    static {
        BASE.put(Role.VIEWER, EnumSet.of(Access.VIEW));
        BASE.put(Role.COMMENTER, EnumSet.of(Access.VIEW, Access.COMMENT));
        BASE.put(Role.EDITOR, EnumSet.of(Access.VIEW, Access.COMMENT, Access.EDIT));
        BASE.put(Role.MANAGER, EnumSet.of(Access.VIEW, Access.COMMENT, Access.EDIT, Access.SHARE));
        BASE.put(Role.OWNER, EnumSet.allOf(Access.class));
    }

    private Grants() {}

    /** The base grants of a role, as every caller sees them. */
    public static Set<Access> base(Role role) {
        return Collections.unmodifiableSet(BASE.get(role));
    }

    /** What a user with this role and these extra grants may do. */
    public static Set<Access> effective(Role role, Set<Access> extra) {
        EnumSet<Access> all = EnumSet.noneOf(Access.class);
        all.addAll(BASE.get(role));
        all.addAll(extra);
        return all;
    }

    /** The grants held in a value read from the extra_grants column. */
    public static Set<Access> decode(String column) {
        EnumSet<Access> grants = EnumSet.noneOf(Access.class);
        for (String name : column.split(",")) {
            if (!name.isEmpty()) grants.add(Access.valueOf(name));
        }
        return grants;
    }
}
