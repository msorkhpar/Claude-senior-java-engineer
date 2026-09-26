package practice;

import java.util.Optional;

public final class EnumStore {

    private EnumStore() {
    }

    /** The first release. */
    public enum StatusV1 { ACTIVE, INACTIVE }

    /** A later release that added PENDING in front. */
    public enum StatusV2 { PENDING, ACTIVE, INACTIVE }

    /** The text to persist for {@code constant}. */
    public static String store(Enum<?> constant) {
        return String.valueOf(constant.ordinal());
    }

    /** The constant of {@code type} that {@code stored} names, or empty when it is null or names none. */
    public static <E extends Enum<E>> Optional<E> load(Class<E> type, String stored) {
        if (stored == null) {
            return Optional.empty();
        }
        E[] all = type.getEnumConstants();
        int ordinal = Integer.parseInt(stored);
        return ordinal < all.length ? Optional.of(all[ordinal]) : Optional.empty();
    }
}
