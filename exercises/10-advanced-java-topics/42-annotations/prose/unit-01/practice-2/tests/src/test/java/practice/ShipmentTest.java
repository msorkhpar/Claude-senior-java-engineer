package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShipmentTest {

    @TempDir
    Path out;

    /** Generic code that ships one parcel of any type, as other modules write it. */
    static <T> Shipment<T> one(T parcel) {
        return Shipment.of(parcel);
    }

    @Test
    void keepsTheParcelsInOrder() {
        Shipment<String> s = Shipment.of(new String("crate-7"), new String("pallet-2"), new String("box-19"));
        assertThat(s.parcels()).containsExactly("crate-7", "pallet-2", "box-19");
        assertThat(s.size()).isEqualTo(3);
    }

    @Test
    void genericCodeGetsAShipmentOfItsType() {
        Shipment<Integer> s = one(40_017);
        Integer first = s.parcels().get(0);
        assertThat(first).isEqualTo(40_017);
    }

    @Test
    void genericCallersCompileWithoutUncheckedWarnings() throws Exception {
        String source = """
                package probe;

                import practice.Shipment;

                public class Dispatch {
                    static <P> Shipment<P> single(P parcel) {
                        return Shipment.of(parcel);
                    }
                }
                """;
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assertThat(javac).as("a JDK compiler").isNotNull();
        Path classes = Path.of(Shipment.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///probe/Dispatch.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<String> options = List.of("-Xlint:unchecked", "-proc:none",
                "-classpath", classes.toString(), "-d", out.toString());
        try (StandardJavaFileManager files = javac.getStandardFileManager(diagnostics, null, null)) {
            boolean compiled = javac.getTask(new StringWriter(), files, diagnostics, options, null, List.of(file)).call();
            assertThat(compiled).as("the generic caller compiles").isTrue();
        }
        List<String> unchecked = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            if (d.getKind() != Diagnostic.Kind.ERROR && String.valueOf(d.getCode()).contains("unchecked")) {
                unchecked.add(d.getMessage(null));
            }
        }
        assertThat(unchecked).as("unchecked warnings at the caller").isEmpty();
    }

    @Test
    void reusingTheCallersArrayDoesNotChangeAShipment() {
        String[] loading = {new String("crate-7"), new String("pallet-2")};
        Shipment<String> first = Shipment.of(loading);
        loading[0] = new String("crate-8");
        loading[1] = new String("pallet-3");
        Shipment<String> second = Shipment.of(loading);
        assertThat(first.parcels()).containsExactly("crate-7", "pallet-2");
        assertThat(second.parcels()).containsExactly("crate-8", "pallet-3");
    }
}
