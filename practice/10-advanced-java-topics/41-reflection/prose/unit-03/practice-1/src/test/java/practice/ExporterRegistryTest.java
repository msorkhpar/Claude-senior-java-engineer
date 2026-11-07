package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExporterRegistryTest {

    /** Static setup that ran, recorded here so reading it doesn't set up the exporter itself. */
    static final List<String> SETUP_RAN = new CopyOnWriteArrayList<>();

    public static class HtmlExporter implements ExporterRegistry.Exporter {
        public String export(String report) { return "<h1>" + report + "</h1>"; }
    }

    public static class MarkdownExporter implements ExporterRegistry.Exporter {
        public String export(String report) { return "# " + report; }
    }

    /** An exporter whose class sets up a template cache when it is initialised. */
    public static class LedgerExporter implements ExporterRegistry.Exporter {
        static { SETUP_RAN.add("ledger"); }
        public String export(String report) { return "ledger:" + report; }
    }

    /** A class from the same code base that is not an exporter. */
    public static class NightlyCleanup {
        public void run() { }
    }

    private static final ClassLoader LOADER = ExporterRegistryTest.class.getClassLoader();

    private static Map<String, String> config(String... keysAndNames) {
        Map<String, String> config = new LinkedHashMap<>();
        for (int i = 0; i < keysAndNames.length; i += 2) config.put(keysAndNames[i], keysAndNames[i + 1]);
        return config;
    }

    @Test
    void createsTheConfiguredExporterForEachKey() {
        ExporterRegistry registry = new ExporterRegistry(config(
                "html", HtmlExporter.class.getName(),
                "md", MarkdownExporter.class.getName()), LOADER);

        assertThat(registry.create("html").export("Q3 revenue")).isEqualTo("<h1>Q3 revenue</h1>");
        assertThat(registry.create("md").export("Q3 revenue")).isEqualTo("# Q3 revenue");
        assertThat(registry.create("html")).isNotSameAs(registry.create("html"));
    }

    @Test
    void aMisspeltClassStopsTheRegistryFromBeingBuilt() {
        Map<String, String> config = config(
                "html", HtmlExporter.class.getName(),
                "md", "practice.ExporterRegistryTest$MarkdwonExporter");

        assertThatThrownBy(() -> new ExporterRegistry(config, LOADER))
                .isInstanceOf(ExporterRegistry.ConfigException.class)
                .hasMessageContaining("md");
    }

    @Test
    void aClassThatIsNotAnExporterStopsTheRegistryFromBeingBuilt() {
        Map<String, String> config = config(
                "cleanup", NightlyCleanup.class.getName(),
                "html", HtmlExporter.class.getName());

        assertThatThrownBy(() -> new ExporterRegistry(config, LOADER))
                .isInstanceOf(ExporterRegistry.ConfigException.class)
                .hasMessageContaining("cleanup");
    }

    @Test
    void buildingTheRegistryRunsNoExportersSetup() {
        ExporterRegistry registry = new ExporterRegistry(config(
                "ledger", "practice.ExporterRegistryTest$LedgerExporter"), LOADER);

        assertThat(SETUP_RAN).as("setup that ran while the registry was built").isEmpty();

        assertThat(registry.create("ledger").export("May")).isEqualTo("ledger:May");
        assertThat(SETUP_RAN).containsExactly("ledger");
    }
}
