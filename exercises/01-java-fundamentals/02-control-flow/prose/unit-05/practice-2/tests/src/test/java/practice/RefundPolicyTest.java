package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static practice.RefundPolicy.Reason.*;

class RefundPolicyTest {

    private static final String SOURCE = "src/main/java/practice/RefundPolicy.java";
    private static final String NEW_REASON = "STORE_CREDIT_SWAP";

    @TempDir
    Path out;

    @Test
    void givesEachReasonItsWindow() {
        assertThat(RefundPolicy.windowDays(WRONG_ITEM)).isEqualTo(60);
        assertThat(RefundPolicy.windowDays(LATE_DELIVERY)).isEqualTo(30);
        assertThat(RefundPolicy.windowDays(CHANGED_MIND)).isEqualTo(14);
        assertThat(RefundPolicy.windowDays(DAMAGED)).isEqualTo(60);
        assertThat(RefundPolicy.windowDays(NOT_AS_DESCRIBED)).isEqualTo(30);
    }

    @Test
    void aNewReasonStopsTheBuildUntilSomeoneGivesItAWindow() throws Exception {
        String source = readSource();
        assertThat(errors(compile(source, out.resolve("now")))).as("the file compiles as it is").isEmpty();

        String withNewReason = source.replaceFirst("enum Reason\\s*\\{", "enum Reason { " + NEW_REASON + ",");
        assertThat(withNewReason).as("the Reason enum was found").isNotEqualTo(source);

        List<String> codes = errors(compile(withNewReason, out.resolve("next")));
        assertThat(codes).as("compiler errors once the returns service adds " + NEW_REASON)
                .contains("compiler.err.not.exhaustive");
    }

    @Test
    void aBuildFromBeforeANewReasonRefusesItInsteadOfAnswering() throws Exception {
        Path oldBuild = out.resolve("old");
        assertThat(errors(compile(readSource(), oldBuild))).as("the file compiles as it is").isEmpty();

        // The returns service ships its new enum; this class is not rebuilt.
        String newEnum = "package practice; public final class RefundPolicy { public enum Reason { "
                + "DAMAGED, WRONG_ITEM, NOT_AS_DESCRIBED, LATE_DELIVERY, CHANGED_MIND, " + NEW_REASON + " } }";
        Path newBuild = out.resolve("new");
        assertThat(errors(compile(newEnum, newBuild))).isEmpty();
        Files.copy(newBuild.resolve("practice/RefundPolicy$Reason.class"),
                oldBuild.resolve("practice/RefundPolicy$Reason.class"), StandardCopyOption.REPLACE_EXISTING);

        try (URLClassLoader loader = new URLClassLoader(new URL[]{oldBuild.toUri().toURL()},
                ClassLoader.getPlatformClassLoader())) {
            Class<?> reason = loader.loadClass("practice.RefundPolicy$Reason");
            Method windowDays = loader.loadClass("practice.RefundPolicy").getMethod("windowDays", reason);

            assertThat(windowDays.invoke(null, reasonNamed(reason, "NOT_AS_DESCRIBED"))).isEqualTo(30);
            Object newReason = reasonNamed(reason, NEW_REASON);
            assertThatThrownBy(() -> windowDays.invoke(null, newReason))
                    .as("windowDays(" + NEW_REASON + ") on the old build")
                    .isInstanceOf(InvocationTargetException.class);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object reasonNamed(Class<?> reason, String name) {
        return Enum.valueOf((Class) reason, name);
    }

    private static String readSource() throws Exception {
        Path base = Path.of(System.getProperty("basedir", System.getProperty("user.dir")));
        return Files.readString(base.resolve(SOURCE));
    }

    private static List<Diagnostic<? extends JavaFileObject>> compile(String source, Path out) throws Exception {
        Files.createDirectories(out);
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assertThat(javac).as("a Java compiler in this JDK").isNotNull();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///practice/RefundPolicy.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        javac.getTask(null, null, diagnostics, List.of("--release", "21", "-proc:none", "-d", out.toString()),
                null, List.of(file)).call();
        return diagnostics.getDiagnostics();
    }

    private static List<String> errors(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        return diagnostics.stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(Diagnostic::getCode)
                .toList();
    }
}
