package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads the header of compiled classes and decides whether a given Java runtime will load them.
 * The platform team runs it over every jar before a service moves to a new Java release.
 */
public class ClassFileCheck {

    /** The unsigned 16-bit big-endian value at {@code offset}. */
    static int u2(byte[] classFile, int offset) {
        return ((classFile[offset] & 0xFF) << 8) | (classFile[offset + 1] & 0xFF);
    }

    static int minorVersion(byte[] classFile) {
        return u2(classFile, 4);
    }

    static int majorVersion(byte[] classFile) {
        return u2(classFile, 6);
    }

    /** The Java feature release the class was compiled for: 17, 21, 25, ... */
    static int release(byte[] classFile) {
        // TODO: Answer with the feature release (8, 17, 21, ...) this class was compiled for.
        throw new IllegalStateException("TODO: release");
    }

    /** Whether the class was compiled with preview features enabled. */
    static boolean usesPreview(byte[] classFile) {
        // TODO: Answer whether this class was compiled with preview features enabled.
        throw new IllegalStateException("TODO: usesPreview");
    }

    /**
     * Whether a JVM of feature release {@code runtimeRelease}, started with or without
     * {@code --enable-preview}, loads this class.
     */
    static boolean loads(byte[] classFile, int runtimeRelease, boolean previewEnabled) {
        // TODO: Answer whether this runtime, with or without --enable-preview, accepts the class
        //       instead of failing with UnsupportedClassVersionError.
        throw new IllegalStateException("TODO: loads");
    }

    /** The names of the classes, in name order, that the runtime would refuse to load. */
    public static List<String> refused(Map<String, byte[]> classes, int runtimeRelease, boolean previewEnabled) {
        List<String> refused = new ArrayList<>();
        new TreeMap<>(classes).forEach((name, bytes) -> {
            if (!loads(bytes, runtimeRelease, previewEnabled)) refused.add(name);
        });
        return refused;
    }

    /** A one-line description of a class for the report, e.g. "release 21, preview". */
    public static String describe(byte[] classFile) {
        return "release " + release(classFile) + (usesPreview(classFile) ? ", preview" : "");
    }
}
