package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecentFilesTest {

    private static final Duration LIMIT = Duration.ofSeconds(10);

    private static RecentFiles opened(int capacity, String... paths) {
        RecentFiles recent = new RecentFiles(capacity);
        for (String p : paths) recent.open(p);
        return recent;
    }

    @Test
    void showsOpenedFilesMostRecentFirst() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RecentFiles recent = opened(5, "/path/to/project/notes.md", "/path/to/project/pom.xml",
                    "/path/to/project/App.java");
            assertThat(recent.menu()).containsExactly("/path/to/project/App.java",
                    "/path/to/project/pom.xml", "/path/to/project/notes.md");
            assertThat(recent.mostRecent()).contains("/path/to/project/App.java");
        });
    }

    @Test
    void reopeningAFileMovesItToTheTop() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RecentFiles recent = opened(5, "q1.csv", "q2.csv", "q3.csv", "q4.csv", "q2.csv");
            assertThat(recent.menu()).containsExactly("q2.csv", "q4.csv", "q3.csv", "q1.csv");
            assertThat(recent.mostRecent()).contains("q2.csv");
        });
    }

    @Test
    void aFreshInstallHasNothingToReopen() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RecentFiles recent = new RecentFiles(4);
            assertThat(recent.mostRecent()).isEqualTo(Optional.empty());
            assertThat(recent.menu()).isEmpty();
        });
    }

    @Test
    void aFullMenuDropsTheFileOpenedLongestAgo() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RecentFiles recent = opened(2, "draft-1.txt", "draft-2.txt", "draft-3.txt");
            assertThat(recent.menu()).containsExactly("draft-3.txt", "draft-2.txt");
            recent.open("draft-4.txt");
            assertThat(recent.menu()).containsExactly("draft-4.txt", "draft-3.txt");
        });
    }
}
