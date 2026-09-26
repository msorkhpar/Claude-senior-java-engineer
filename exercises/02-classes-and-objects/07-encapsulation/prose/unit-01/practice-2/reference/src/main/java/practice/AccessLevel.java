package practice;

import java.lang.reflect.Modifier;

/** The four access levels, declared in alphabetical order. */
public enum AccessLevel {
    PACKAGE, PRIVATE, PROTECTED, PUBLIC;

    /** The level a modifier bit set declares. */
    public static AccessLevel of(int modifiers) {
        if (Modifier.isPublic(modifiers)) {
            return PUBLIC;
        }
        if (Modifier.isProtected(modifiers)) {
            return PROTECTED;
        }
        if (Modifier.isPrivate(modifiers)) {
            return PRIVATE;
        }
        return PACKAGE;
    }

    /** How restrictive a level is: a higher number allows fewer places. */
    private int restriction() {
        return switch (this) {
            case PUBLIC -> 0;
            case PROTECTED -> 1;
            case PACKAGE -> 2;
            case PRIVATE -> 3;
        };
    }

    /** Whether this level allows access to fewer places than {@code other}. */
    public boolean isMoreRestrictiveThan(AccessLevel other) {
        return restriction() > other.restriction();
    }

    /** The most restrictive of the levels given. */
    public static AccessLevel mostRestrictive(AccessLevel first, AccessLevel... rest) {
        AccessLevel most = first;
        for (AccessLevel level : rest) {
            if (level.isMoreRestrictiveThan(most)) {
                most = level;
            }
        }
        return most;
    }
}
