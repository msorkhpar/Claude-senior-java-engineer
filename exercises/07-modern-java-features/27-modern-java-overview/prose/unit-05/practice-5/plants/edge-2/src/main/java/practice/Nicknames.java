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
        return nickname.codePoints().anyMatch(Character::isEmoji);
    }

    /** A line of '-' to print under the nickname: one '-' for each character it shows. */
    public static String underline(String nickname) {
        return "-".repeat(nickname.codePointCount(0, nickname.length()));
    }
}
