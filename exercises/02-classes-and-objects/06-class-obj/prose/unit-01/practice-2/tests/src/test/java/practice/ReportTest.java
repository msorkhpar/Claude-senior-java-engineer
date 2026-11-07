package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTest {

    private static Report report(String currency) {
        return new Report(currency, new byte[400_000]);
    }

    private static byte[] serialize(Object o) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(o);
        }
        return bytes.toByteArray();
    }

    private static Object deserialize(byte[] b) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return in.readObject();
        }
    }

    @Test
    void aRowIsPricedInItsReportsCurrency() {
        Report.Row chf = report("CHF").row("lamp-204", 4_905);
        Report.Row sek = report("SEK").row("desk-77", 129_900);

        assertThat(chf.sku()).isEqualTo("lamp-204");
        assertThat(chf.price()).isEqualTo("49.05 CHF");
        assertThat(sek.price()).isEqualTo("1299.00 SEK");
    }

    @Test
    void aRowSurvivesTheSessionStore() throws Exception {
        Report.Row row = report("NOK").row("chair-3", 31_550);

        Report.Row back = (Report.Row) deserialize(serialize(row));

        assertThat(back.sku()).isEqualTo("chair-3");
        assertThat(back.price()).isEqualTo("315.50 NOK");
    }

    @Test
    void aStoredRowCarriesNoneOfItsReport() throws Exception {
        Report.Row row = report("DKK").row("shelf-12", 8_800);

        assertThat(serialize(row).length).isLessThan(4_000);
    }
}
