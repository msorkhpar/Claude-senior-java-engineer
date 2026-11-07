package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DelimitedFileTest {

    @Test
    void readsSemicolonSeparatedLines() {
        DelimitedFile file = new DelimitedFile(';', 3);

        List<List<String>> rows = file.read(List.of("SKU-310;Desk lamp;19.90", "SKU-311;Shelf;54.00"));

        assertThat(rows).containsExactly(List.of("SKU-310", "Desk lamp", "19.90"), List.of("SKU-311", "Shelf", "54.00"));
        assertThat(file.rejected()).isEmpty();
    }

    @Test
    void rejectsALineWithTooFewOrTooManyFields() {
        DelimitedFile file = new DelimitedFile(',', 3);

        List<List<String>> rows = file.read(List.of("SKU-9,Rug", "SKU-10,Mat,4.50", "SKU-11,Hook,1.20,extra"));

        assertThat(rows).containsExactly(List.of("SKU-10", "Mat", "4.50"));
        assertThat(file.rejected()).containsExactly("SKU-9,Rug", "SKU-11,Hook,1.20,extra");
    }

    @Test
    void anEmptyColumnInTheMiddleIsAField() {
        DelimitedFile file = new DelimitedFile('\t', 3);

        assertThat(file.read(List.of("SKU-12\t\t7.25"))).containsExactly(List.of("SKU-12", "", "7.25"));
    }

    @Test
    void aSupplierThatSeparatesWithAPipeIsReadTheSameWay() {
        DelimitedFile file = new DelimitedFile('|', 3);

        List<List<String>> rows = file.read(List.of("SKU-401|Kettle|32.00"));

        assertThat(rows).containsExactly(List.of("SKU-401", "Kettle", "32.00"));
        assertThat(file.rejected()).isEmpty();
    }

    @Test
    void anEmptyLastColumnIsAField() {
        DelimitedFile file = new DelimitedFile(';', 4);

        List<List<String>> rows = file.read(List.of("SKU-77;Vase;12.00;", "SKU-78;Bowl;;"));

        assertThat(rows).containsExactly(List.of("SKU-77", "Vase", "12.00", ""), List.of("SKU-78", "Bowl", "", ""));
        assertThat(file.rejected()).isEmpty();
    }
}
