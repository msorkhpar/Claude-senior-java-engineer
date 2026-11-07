package practice;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Imports the order documents that partners upload into the import directory. */
public class OrderImport {

    /** One partner order: where it came from, its item codes, and the buyer's note. */
    public record Order(String channel, List<String> items, String note) {}

    /** Parses one uploaded document. A document the parser refuses ends in an exception. */
    public Order read(Path file) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = newFactory().newDocumentBuilder();
        Document doc = builder.parse(file.toFile());
        Element order = doc.getDocumentElement();
        List<String> items = new ArrayList<>();
        NodeList nodes = order.getElementsByTagName("item");
        for (int i = 0; i < nodes.getLength(); i++) items.add(nodes.item(i).getTextContent().strip());
        NodeList notes = order.getElementsByTagName("note");
        String note = notes.getLength() == 0 ? "" : notes.item(0).getTextContent().strip();
        return new Order(order.getAttribute("channel"), items, note);
    }

    /**
     * The factory every upload is parsed with. Partner orders are plain XML: elements, attributes
     * and text, and never a DOCTYPE.
     */
    static DocumentBuilderFactory newFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory;
    }
}
