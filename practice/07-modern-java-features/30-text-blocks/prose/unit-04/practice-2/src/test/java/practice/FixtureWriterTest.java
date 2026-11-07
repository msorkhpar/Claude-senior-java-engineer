package practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureWriterTest {

    private static Path work;
    private static final AtomicInteger probes = new AtomicInteger();

    @BeforeAll
    static void workDir() throws IOException {
        work = Files.createTempDirectory("fixture-writer");
    }

    @AfterAll
    static void cleanUp() throws IOException {
        try (Stream<Path> files = Files.walk(work)) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    /** Compiles the generated text block with javac and returns the value it holds. */
    private static String compiled(String value) throws Exception {
        String block = FixtureWriter.textBlock(value);
        String name = "Probe" + probes.incrementAndGet();
        Path dir = Files.createDirectories(work.resolve(name));
        Path file = dir.resolve(name + ".java");
        Files.writeString(file, "public class " + name + " {\n    public static final String VALUE = "
                + block + ";\n}\n", StandardCharsets.UTF_8);
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int status = javac.run(null, errors, errors, "-encoding", "UTF-8", "-d", dir.toString(), file.toString());
        assertThat(status).as("javac rejected the generated source:%n%s%n%s", block, errors).isZero();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null)) {
            return (String) loader.loadClass(name).getField("VALUE").get(null);
        }
    }

    @Test
    void aRecordedJsonResponseComesBackUnchanged() throws Exception {
        String json = "{\n  \"order\": 4417,\n  \"status\": \"SHIPPED\",\n  \"note\": \"\"\n}\n";

        assertThat(compiled(json)).isEqualTo(json);
    }

    @Test
    void aValueWithoutAFinalLineBreakOrWithBlankLinesComesBackUnchanged() throws Exception {
        String text = "Dear Jane Doe,\n\n    your parcel is on its way.\n\nRegards";

        assertThat(compiled(text)).isEqualTo(text);
        assertThat(compiled("")).isEqualTo("");
    }

    @Test
    void backslashesComeBackAsBackslashes() throws Exception {
        String text = "share: \\\\files01\\new\\table.csv\npattern: \\d{4}-\\d{2} \\u0041\nC:\\";

        assertThat(compiled(text)).isEqualTo(text);
    }

    @Test
    void runsOfQuotesComeBackUnchanged() throws Exception {
        String text = "docstring: \"\"\"Return the total.\"\"\"\nempty: \"\" and \"\"\"\"\"\"\"\nends with \"";

        assertThat(compiled(text)).isEqualTo(text);
    }

    @Test
    void spacesAtTheEndOfALineComeBackUnchanged() throws Exception {
        String text = "CODE  QTY  \nA-1   12   \n   \nend";

        assertThat(compiled(text)).isEqualTo(text);
    }

    @Test
    void carriageReturnsComeBackUnchanged() throws Exception {
        String text = "220 mail.example.com ready\r\n250 OK\r\n";

        assertThat(compiled(text)).isEqualTo(text);
    }
}
