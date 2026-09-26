package practice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafePathsTest {

    @TempDir
    Path temp;

    private Path base;

    @BeforeEach
    void createBase() throws IOException {
        base = Files.createDirectory(temp.resolve("base"));
        Files.createDirectory(temp.resolve("base-secrets"));
        Files.writeString(temp.resolve("outside.txt"), "outside", StandardCharsets.UTF_8);
    }

    private void refused(String userSupplied) {
        assertThatThrownBy(() -> SafePaths.resolve(base, userSupplied))
                .as(userSupplied)
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void resolvesNamesInsideTheBase() {
        assertThat(SafePaths.resolve(base, "reports/2024.txt"))
                .isEqualTo(base.resolve("reports").resolve("2024.txt"));
        assertThat(SafePaths.resolve(base, "./a/../b.txt")).isEqualTo(base.resolve("b.txt"));
        assertThat(SafePaths.resolve(base, "a/./b/../c.txt")).isEqualTo(base.resolve("a").resolve("c.txt"));
    }

    @Test
    void theCheckRunsOnTheNormalizedPath() {
        refused("../../etc/passwd");
        refused("docs/../../outside.txt");
        refused("./../outside.txt");
        refused("docs/../../not-yet-written.txt");
    }

    @Test
    void aSiblingWithTheSamePrefixIsOutside() {
        refused("../base-secrets/key.txt");
    }

    @Test
    void aLinkThatLeadsOutsideIsRefused() throws IOException {
        Path elsewhere = Files.createDirectory(temp.resolve("elsewhere"));
        Files.createSymbolicLink(base.resolve("link"), elsewhere);
        Files.writeString(elsewhere.resolve("data.txt"), "data", StandardCharsets.UTF_8);

        refused("link");
        refused("link/data.txt");
    }
}
