package practice;

/** Writes one entry of the release-notes feed that the changelog page publishes. */
public class FeedEntry {

    static final String ENTRY = """
            <entry>
              <title>%s</title>
              <author name="%s"/>
              <link href="%s"/>
            </entry>
            """;

    /** The entry for one release note. Titles and author names are typed by the release managers. */
    public static String xml(String title, String author, String link) {
        return ENTRY.formatted(escape(title), escape(author), escape(link));
    }

    /** text, written so that an XML parser reads it back as the same text. */
    static String escape(String text) {
        // TODO: Release managers type whatever they like, including characters that mean
        //       something in XML. Return the text so that it reads back unchanged, in an
        //       element and in an attribute value.
        throw new IllegalStateException("TODO: escape");
    }
}
