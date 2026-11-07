package practice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PluginLoaderTest {

    /** The server's plugin API. */
    public interface ReportApiExporter {
        String export(String report);
    }

    /** A library both the server and the plugin use; each has its own version of it. */
    public static class Formatter {
        public static String version() {
            return "server-4.1";
        }
    }

    /** The customer's plugin. */
    public static class LedgerPlugin implements ReportApiExporter {
        @Override
        public String export(String report) {
            return report + ".xlsx via formatter " + Formatter.version();
        }
    }

    private static final String API_PREFIX = PluginLoaderTest.class.getName() + "$ReportApi";
    private static final String PLUGIN = LedgerPlugin.class.getName();

    /** The plugin's jar: the plugin, its copy of the API, and its own Formatter, version plugin-3.9. */
    private static Map<String, byte[]> pluginJar() throws IOException {
        Map<String, byte[]> jar = new HashMap<>();
        jar.put(PLUGIN, bytesOf(LedgerPlugin.class));
        jar.put(ReportApiExporter.class.getName(), bytesOf(ReportApiExporter.class));
        jar.put(Formatter.class.getName(), withVersion(bytesOf(Formatter.class), "server-4.1", "plugin-3.9"));
        return jar;
    }

    private static byte[] bytesOf(Class<?> c) throws IOException {
        String resource = c.getName().substring(c.getPackageName().length() + 1) + ".class";
        try (InputStream in = c.getResourceAsStream(resource)) {
            return in.readAllBytes();
        }
    }

    /** Replaces a string constant of the same length in a class file. */
    private static byte[] withVersion(byte[] classFile, String from, String to) {
        byte[] a = from.getBytes(StandardCharsets.UTF_8);
        byte[] b = to.getBytes(StandardCharsets.UTF_8);
        byte[] out = classFile.clone();
        int found = 0;
        for (int i = 0; i + a.length <= out.length; i++) {
            boolean match = true;
            for (int j = 0; j < a.length && match; j++) match = out[i + j] == a[j];
            if (match) {
                System.arraycopy(b, 0, out, i, b.length);
                found++;
            }
        }
        if (found != 1) throw new IllegalStateException("expected one " + from + ", found " + found);
        return out;
    }

    private static PluginLoader newLoader() throws IOException {
        return new PluginLoader(PluginLoaderTest.class.getClassLoader(), pluginJar(), Set.of(API_PREFIX));
    }

    private static Object newPlugin(PluginLoader loader) throws Exception {
        return loader.loadClass(PLUGIN).getDeclaredConstructor().newInstance();
    }

    private static String callExport(Object plugin, String report) throws Exception {
        return (String) plugin.getClass().getMethod("export", String.class).invoke(plugin, report);
    }

    @Test
    void loadsThePluginAndRunsIt() throws Exception {
        PluginLoader loader = newLoader();

        Object plugin = newPlugin(loader);

        assertThat(plugin.getClass().getName()).isEqualTo(PLUGIN);
        assertThat(callExport(plugin, "payroll-2024-11")).startsWith("payroll-2024-11.xlsx via formatter ");
        assertThat(loader.loadClass("java.util.List")).isSameAs(java.util.List.class);
    }

    @Test
    void theServerCanUseThePluginThroughItsApi() throws Exception {
        Object plugin = newPlugin(newLoader());

        assertThat(plugin).isInstanceOf(ReportApiExporter.class);
        ReportApiExporter exporter = (ReportApiExporter) plugin;
        assertThat(exporter.export("stock-week-07")).startsWith("stock-week-07.xlsx");
    }

    @Test
    void thePluginRunsOnTheLibraryVersionItShipsWith() throws Exception {
        Object plugin = newPlugin(newLoader());

        assertThat(callExport(plugin, "fleet-q2")).isEqualTo("fleet-q2.xlsx via formatter plugin-3.9");
    }

    @Test
    void askingForAClassAgainGivesTheSameClass() throws Exception {
        PluginLoader loader = newLoader();
        Class<?> first = loader.loadClass(PLUGIN);

        assertThatCode(() -> loader.loadClass(PLUGIN)).doesNotThrowAnyException();
        assertThat(loader.loadClass(PLUGIN)).isSameAs(first);
        assertThat(loader.loadClass(Formatter.class.getName())).isSameAs(loader.loadClass(Formatter.class.getName()));
    }
}
