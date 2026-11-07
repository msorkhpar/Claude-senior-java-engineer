package practice;

/**
 * Loads test fixtures that used to be text blocks in the test sources. Each fixture file holds a
 * block's body exactly as it was written: everything after the line of the opening delimiter, up
 * to the closing delimiter.
 */
public class FixtureLoader {

    /** The string javac made of a text block with this body. */
    public static String load(String body) {
        // TODO: Give the fixture the value its text block had: the same steps javac takes, in
        //       the same order.
        throw new IllegalStateException("TODO: load");
    }

    /** The text with every Unicode escape replaced by its character, as javac does it. */
    static String unicodeEscapes(String text) {
        // TODO: Fixture authors wrote characters such as \u00e9 as Unicode escapes. Replace each
        //       one the way javac does when it reads a source file.
        throw new IllegalStateException("TODO: unicodeEscapes");
    }
}
