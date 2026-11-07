package practice;

/** Checks and decorates the nicknames users pick for a chat service. */
public final class Nicknames {

    private Nicknames() {
    }

    /**
     * Whether the nickname contains a picture character: a face, a heart, a rocket and the like.
     * Letters of any script, digits, '#', '*', '_' and '-' are not pictures.
     */
    public static boolean hasPicture(String nickname) {
        // TODO: true when the nickname has a picture in it.
        throw new IllegalStateException("TODO: look for a picture");
    }

    /** A line of '-' to print under the nickname: one '-' for each character it shows. */
    public static String underline(String nickname) {
        // TODO: As wide as the nickname looks.
        throw new IllegalStateException("TODO: the underline");
    }
}
