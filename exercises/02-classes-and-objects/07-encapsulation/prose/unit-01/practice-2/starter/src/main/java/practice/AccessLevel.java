package practice;

import java.lang.reflect.Modifier;

/** The four access levels, declared in alphabetical order. */
public enum AccessLevel {
    PACKAGE, PRIVATE, PROTECTED, PUBLIC;

    /** The level a modifier bit set declares. */
    public static AccessLevel of(int modifiers) {
        throw new UnsupportedOperationException("write of");
    }

    /** Whether this level allows access to fewer places than {@code other}. */
    public boolean isMoreRestrictiveThan(AccessLevel other) {
        throw new UnsupportedOperationException("write isMoreRestrictiveThan");
    }

    /** The most restrictive of the levels given. */
    public static AccessLevel mostRestrictive(AccessLevel first, AccessLevel... rest) {
        throw new UnsupportedOperationException("write mostRestrictive");
    }
}
