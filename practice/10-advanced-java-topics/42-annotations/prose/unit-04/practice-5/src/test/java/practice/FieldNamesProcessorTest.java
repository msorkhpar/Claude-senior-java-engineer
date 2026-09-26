package practice;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class FieldNamesProcessorTest {

    @TempDir
    Path out;

    record Compiled(boolean ok, List<Diagnostic<? extends JavaFileObject>> diagnostics, Path classes) {
        Object names(String className) throws Exception {
            try (URLClassLoader loader = new URLClassLoader(new URL[] {classes.toUri().toURL()},
                    FieldNamesProcessorTest.class.getClassLoader())) {
                return loader.loadClass(className).getField("NAMES").get(null);
            }
        }
    }

    private static JavaFileObject source(String path, String code) {
        return new SimpleJavaFileObject(URI.create("string:///" + path), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return code;
            }
        };
    }

    private Compiled compile(JavaFileObject... sources) throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Path classes = Files.createDirectories(out.resolve("classes"));
        Path generated = Files.createDirectories(out.resolve("generated"));
        String here = Path.of(FieldNamesProcessor.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .toString();
        try (StandardJavaFileManager files = javac.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            files.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classes));
            files.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(generated));
            JavaCompiler.CompilationTask task = javac.getTask(null, files, diagnostics,
                    List.of("-proc:full", "-classpath", here), null, List.of(sources));
            task.setProcessors(List.of(new FieldNamesProcessor()));
            boolean ok = task.call();
            return new Compiled(ok, new ArrayList<>(diagnostics.getDiagnostics()), classes);
        }
    }

    @Test
    void generatesTheFieldNamesOfAClass() throws Exception {
        Compiled result = compile(source("Point.java", """
                import practice.FieldNamesProcessor.FieldNames;
                @FieldNames
                class Point {
                    int x;
                    int y;
                    String label;
                    int length() { return label.length(); }
                }
                """));

        assertThat(result.ok()).as("%s", result.diagnostics()).isTrue();
        assertThat(result.names("PointFields")).isEqualTo(List.of("x", "y", "label"));
    }

    @Test
    void theGeneratedClassSitsInTheMarkedClassesPackage() throws Exception {
        Compiled result = compile(source("demo/geo/Place.java", """
                package demo.geo;
                import practice.FieldNamesProcessor.FieldNames;
                @FieldNames
                class Place {
                    String name;
                    double lat;
                }
                """));

        assertThat(result.ok()).as("%s", result.diagnostics()).isTrue();
        assertThat(result.names("demo.geo.PlaceFields")).isEqualTo(List.of("name", "lat"));
    }

    @Test
    void staticFieldsAreLeftOut() throws Exception {
        Compiled result = compile(source("Counter.java", """
                import practice.FieldNamesProcessor.FieldNames;
                @FieldNames
                class Counter {
                    static int COUNT;
                    long total;
                    static final String UNIT = "items";
                    String owner;
                }
                """));

        assertThat(result.ok()).as("%s", result.diagnostics()).isTrue();
        assertThat(result.names("CounterFields")).isEqualTo(List.of("total", "owner"));
    }

    @Test
    void aMarkOnANonClassFailsTheCompilation() throws Exception {
        Compiled result = compile(source("Shape.java", """
                import practice.FieldNamesProcessor.FieldNames;
                @FieldNames
                interface Shape {
                    double area();
                }
                """));

        assertThat(result.ok()).isFalse();
        assertThat(result.diagnostics())
                .anySatisfy(d -> {
                    assertThat(d.getKind()).isEqualTo(Diagnostic.Kind.ERROR);
                    assertThat(d.getMessage(Locale.ROOT)).contains("@FieldNames");
                    assertThat(d.getSource().getName()).endsWith("Shape.java");
                });
    }
}
