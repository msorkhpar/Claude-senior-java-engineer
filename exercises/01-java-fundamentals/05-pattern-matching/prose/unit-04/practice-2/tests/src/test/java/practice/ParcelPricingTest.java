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

class ParcelPricingTest {

    private static final String SOURCE = "src/main/java/practice/ParcelPricing.java";
    private static final String PERMITS = "permits Letter, Box, Tube";
    private static final String NEW_KIND = "public record Crate(int kilos) implements Parcel { }";

    @TempDir
    Path out;

    @Test
    void quotesEveryKindOfParcel() {
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Letter(20))).isEqualTo(140);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Letter(100))).isEqualTo(140);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Letter(101))).isEqualTo(290);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Box(1, false))).isEqualTo(770);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Box(2_000, false))).isEqualTo(890);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Box(2_001, true))).isEqualTo(1_410);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Tube(90))).isEqualTo(980);
        assertThat(ParcelPricing.priceCents(new ParcelPricing.Tube(91))).isEqualTo(1_500);
    }

    @Test
    void aNewKindStopsTheBuildUntilSomeoneGivesItAPrice() throws Exception {
        String source = readSource();
        assertThat(errors(compile(source, out.resolve("now")))).as("the file compiles as it is").isEmpty();

        List<String> codes = errors(compile(withNewKind(source), out.resolve("next")));
        assertThat(codes).as("compiler errors once the carrier adds Crate")
                .contains("compiler.err.not.exhaustive");
    }

    @Test
    void aBuildFromBeforeANewKindRefusesItInsteadOfQuoting() throws Exception {
        Path oldBuild = out.resolve("old");
        String source = readSource();
        assertThat(errors(compile(source, oldBuild))).as("the file compiles as it is").isEmpty();

        // The carrier ships its new hierarchy; this class is not rebuilt.
        String newLibrary = "package practice; public final class ParcelPricing { "
                + "public sealed interface Parcel permits Letter, Box, Tube, Crate { } "
                + "public record Letter(int grams) implements Parcel { } "
                + "public record Box(int grams, boolean fragile) implements Parcel { } "
                + "public record Tube(int lengthCm) implements Parcel { } "
                + NEW_KIND + " }";
        Path newBuild = out.resolve("new");
        assertThat(errors(compile(newLibrary, newBuild))).isEmpty();
        for (String type : List.of("Parcel", "Crate")) {
            Files.copy(newBuild.resolve("practice/ParcelPricing$" + type + ".class"),
                    oldBuild.resolve("practice/ParcelPricing$" + type + ".class"), StandardCopyOption.REPLACE_EXISTING);
        }

        try (URLClassLoader loader = new URLClassLoader(new URL[]{oldBuild.toUri().toURL()},
                ClassLoader.getPlatformClassLoader())) {
            Class<?> parcel = loader.loadClass("practice.ParcelPricing$Parcel");
            Method priceCents = loader.loadClass("practice.ParcelPricing").getMethod("priceCents", parcel);
            Object tube = loader.loadClass("practice.ParcelPricing$Tube").getConstructor(int.class).newInstance(40);
            assertThat(priceCents.invoke(null, tube)).isEqualTo(980);

            Object crate = loader.loadClass("practice.ParcelPricing$Crate").getConstructor(int.class).newInstance(300);
            assertThatThrownBy(() -> priceCents.invoke(null, crate))
                    .as("priceCents(Crate) on the old build")
                    .isInstanceOf(InvocationTargetException.class);
        }
    }

    private static String withNewKind(String source) {
        assertThat(source).as("the Parcel hierarchy in " + SOURCE).contains(PERMITS);
        String added = source.replace(PERMITS, PERMITS + ", Crate");
        int end = added.lastIndexOf('}');
        return added.substring(0, end) + NEW_KIND + "\n}\n";
    }

    private static String readSource() throws Exception {
        Path base = Path.of(System.getProperty("basedir", System.getProperty("user.dir")));
        return Files.readString(base.resolve(SOURCE));
    }

    private static List<Diagnostic<? extends JavaFileObject>> compile(String source, Path out) throws Exception {
        Files.createDirectories(out);
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assertThat(javac).as("a Java compiler in this JDK").isNotNull();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///practice/ParcelPricing.java"),
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
