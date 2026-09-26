package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportersTest {

    @Test
    void eachExporterFillsInItsOwnSteps() {
        Exporters.Exporter csv = new Exporters.Csv();
        assertThat(csv.export(List.of("apple", "pear"))).isEqualTo("index,item\n0,apple\n1,pear\nend\n");
        Exporters.Exporter markdown = new Exporters.Markdown();
        assertThat(markdown.export(List.of("apple", "pear"))).startsWith("# Items\n").endsWith("\n(2 items)\n");
    }

    @Test
    void anEmptyListStillHasHeaderAndFooter() {
        assertThat(new Exporters.Markdown().export(List.of())).isEqualTo("# Items\n(0 items)\n");
    }

    @Test
    void aSubclassMaySkipTheHeader() {
        assertThat(new Exporters.Plain().export(List.of("apple"))).isEqualTo("apple\n--\n");
    }

    @Test
    void markdownNumbersFromOne() {
        assertThat(new Exporters.Markdown().export(List.of("apple", "pear")))
                .isEqualTo("# Items\n1. apple\n2. pear\n(2 items)\n");
    }
}
