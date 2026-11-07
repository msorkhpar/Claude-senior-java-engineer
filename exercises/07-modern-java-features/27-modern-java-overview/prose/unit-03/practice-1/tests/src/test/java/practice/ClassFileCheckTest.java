package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClassFileCheckTest {

    /** The first bytes of a class file: magic, minor version, major version, then a little of the rest. */
    private static byte[] header(int major, int minor) {
        return new byte[] {
                (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
                (byte) (minor >> 8), (byte) minor,
                (byte) (major >> 8), (byte) major,
                0x00, 0x1D, 0x0A, 0x00};
    }

    private static final int PREVIEW = 0xFFFF;

    @Test
    void readsTheReleaseOfAClassTheBuildJustCompiled() throws IOException {
        byte[] compiled;
        try (InputStream in = ClassFileCheck.class.getResourceAsStream("ClassFileCheck.class")) {
            compiled = in.readAllBytes();
        }

        assertThat(ClassFileCheck.describe(compiled)).isEqualTo("release 21");
    }

    @Test
    void readsTheReleaseAndThePreviewMark() {
        assertThat(ClassFileCheck.describe(header(52, 0))).isEqualTo("release 8");
        assertThat(ClassFileCheck.describe(header(61, 0))).isEqualTo("release 17");
        assertThat(ClassFileCheck.describe(header(66, PREVIEW))).isEqualTo("release 22, preview");
    }

    @Test
    void ordinaryClassesLoadOnTheirReleaseAndEveryLaterOne() {
        Map<String, byte[]> jar = Map.of(
                "billing/Invoice", header(55, 0),
                "billing/Ledger", header(61, 0),
                "billing/Tax", header(67, 0));

        assertThat(ClassFileCheck.refused(jar, 23, false)).isEmpty();
        assertThat(ClassFileCheck.refused(jar, 23, true)).isEmpty();
        assertThat(ClassFileCheck.refused(jar, 17, false)).containsExactly("billing/Tax");
    }

    @Test
    void aPreviewClassLoadsOnItsOwnReleaseWhenPreviewsAreEnabled() {
        Map<String, byte[]> jar = Map.of(
                "search/Query", header(66, PREVIEW),
                "search/Index", header(61, 0));

        assertThat(ClassFileCheck.refused(jar, 22, true)).isEmpty();
    }

    @Test
    void aPreviewClassIsRefusedByALaterRelease() {
        Map<String, byte[]> jar = Map.of(
                "search/Query", header(66, PREVIEW),
                "search/Index", header(61, 0));

        assertThat(ClassFileCheck.refused(jar, 23, true)).containsExactly("search/Query");
        assertThat(ClassFileCheck.refused(jar, 24, true)).containsExactly("search/Query");
    }

    @Test
    void aPreviewClassIsRefusedWhenPreviewsAreNotEnabled() {
        Map<String, byte[]> jar = Map.of(
                "search/Query", header(66, PREVIEW),
                "search/Index", header(61, 0));

        assertThat(ClassFileCheck.refused(jar, 22, false)).containsExactly("search/Query");
    }
}
