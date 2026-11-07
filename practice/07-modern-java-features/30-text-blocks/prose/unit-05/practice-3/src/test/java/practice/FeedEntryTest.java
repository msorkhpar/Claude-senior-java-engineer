package practice;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FeedEntryTest {

    /** Parses the entry with the JDK's XML parser; a malformed entry fails the test here. */
    private static Element parse(String xml) throws Exception {
        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());   // throws on a fatal error, prints nothing
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
    }

    private static String title(Element entry) {
        return entry.getElementsByTagName("title").item(0).getTextContent();
    }

    private static String author(Element entry) {
        return ((Element) entry.getElementsByTagName("author").item(0)).getAttribute("name");
    }

    @Test
    void aPlainEntryReadsBackAsWritten() throws Exception {
        String xml = FeedEntry.xml("Release 4.2", "Jane Doe", "https://example.invalid/notes/4.2");

        assertThat(xml).startsWith("<entry>\n  <title>Release 4.2</title>\n");
        Element entry = parse(xml);
        assertThat(title(entry)).isEqualTo("Release 4.2");
        assertThat(author(entry)).isEqualTo("Jane Doe");
        assertThat(((Element) entry.getElementsByTagName("link").item(0)).getAttribute("href"))
                .isEqualTo("https://example.invalid/notes/4.2");
    }

    @Test
    void anAmpersandInATitleReadsBack() throws Exception {
        Element entry = parse(FeedEntry.xml("Fixes for Q&A and R&D", "Jane Doe", "https://example.invalid/n/1"));

        assertThat(title(entry)).isEqualTo("Fixes for Q&A and R&D");
    }

    @Test
    void aTitleAboutMarkupReadsBackAsText() throws Exception {
        Element entry = parse(FeedEntry.xml("Render <br> and &nbsp; in exports", "Jane Doe",
                "https://example.invalid/n/2"));

        assertThat(title(entry)).isEqualTo("Render <br> and &nbsp; in exports");
    }

    @Test
    void aQuoteInAnAuthorNameReadsBack() throws Exception {
        Element entry = parse(FeedEntry.xml("Release 4.3", "Jane \"JD\" Doe", "https://example.invalid/n/3"));

        assertThat(author(entry)).isEqualTo("Jane \"JD\" Doe");
    }
}
