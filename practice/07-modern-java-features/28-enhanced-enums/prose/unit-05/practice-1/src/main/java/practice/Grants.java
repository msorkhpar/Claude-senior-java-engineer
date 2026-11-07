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
        // TODO: Return the role's base grants. Callers across the service receive this set, and
        //       whatever one of them does with it must not change what the role grants.
        throw new IllegalStateException("TODO: base");
    }

    /** What a user with this role and these extra grants may do. */
    public static Set<Access> effective(Role role, Set<Access> extra) {
        // TODO: The role's base grants plus the extra ones. Computing one user's grants must
        //       leave the role, and every other user of it, as they were.
        throw new IllegalStateException("TODO: effective");
    }

    /** The grants held in a value read from the extra_grants column. */
    public static Set<Access> decode(String column) {
        // TODO: Turn a stored value such as "COMMENT,SHARE" back into grants. Every value the
        //       table holds must be readable, including a user's who has no extra grants.
        throw new IllegalStateException("TODO: decode");
    }
}
