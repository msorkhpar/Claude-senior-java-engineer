package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OrderImportTest {

    @TempDir
    Path uploads;

    /** A file on the import host that no upload may ever reveal. */
    private static final String HOST_FILE_TEXT = "import-host-only-7c1";
    /** Declarations a partner keeps beside its orders; the import must never load them. */
    private static final String PARTNER_DTD_CHANNEL = "loaded-from-partner-dtd";

    @BeforeEach
    void putFilesBesideTheUploads() throws IOException {
        Files.writeString(uploads.resolve("host.conf"), HOST_FILE_TEXT);
        Files.writeString(uploads.resolve("partner.dtd"),
                "<!ATTLIST order channel CDATA \"" + PARTNER_DTD_CHANNEL + "\">\n"
                        + "<!ENTITY remark \"" + PARTNER_DTD_CHANNEL + "\">\n");
    }

    private Path upload(String name, String xml) throws IOException {
        return Files.writeString(uploads.resolve(name), xml);
    }

    /** The parser refused the document, or read it without anything from outside it. */
    private void assertNothingFromOutside(Path file) throws Exception {
        OrderImport.Order order;
        try {
            order = new OrderImport().read(file);
        } catch (SAXException refusedByTheParser) {
            return;
        }
        assertThat(order.toString()).doesNotContain(HOST_FILE_TEXT).doesNotContain(PARTNER_DTD_CHANNEL);
    }

    @Test
    void readsAPlainPartnerOrder() throws Exception {
        Path file = upload("order-5521.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <order channel="edi">
                  <item>SKU-0042</item>
                  <item>SKU-0917</item>
                  <note>deliver to dock 2 &amp; call ahead</note>
                </order>
                """);

        OrderImport.Order order = new OrderImport().read(file);

        assertThat(order.channel()).isEqualTo("edi");
        assertThat(order.items()).containsExactly("SKU-0042", "SKU-0917");
        assertThat(order.note()).isEqualTo("deliver to dock 2 & call ahead");
    }

    @Test
    void anEntityThatNamesAFileDoesNotBringItIn() throws Exception {
        Path file = upload("order-5522.xml", """
                <?xml version="1.0"?>
                <!DOCTYPE order [
                  <!ENTITY extra SYSTEM "host.conf">
                ]>
                <order channel="portal"><item>SKU-0042</item><note>&extra;</note></order>
                """);

        assertNothingFromOutside(file);
    }

    @Test
    void aParameterEntityDoesNotLoadDeclarationsFromAFile() throws Exception {
        Path file = upload("order-5523.xml", """
                <?xml version="1.0"?>
                <!DOCTYPE order [
                  <!ENTITY % decl SYSTEM "partner.dtd">
                  %decl;
                ]>
                <order><item>SKU-0042</item><note>&remark;</note></order>
                """);

        assertNothingFromOutside(file);
    }

    @Test
    void anExternalDtdIsNotLoaded() throws Exception {
        Path file = upload("order-5524.xml", """
                <?xml version="1.0"?>
                <!DOCTYPE order SYSTEM "partner.dtd">
                <order><item>SKU-0042</item><note>none</note></order>
                """);

        assertNothingFromOutside(file);
    }
}
