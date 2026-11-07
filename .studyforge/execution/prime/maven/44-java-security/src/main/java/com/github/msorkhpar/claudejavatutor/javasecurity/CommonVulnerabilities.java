package com.github.msorkhpar.claudejavatutor.javasecurity;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The four defences of unit 10.6.2, each as a small method: an ORDER BY allow-list, a path check
 * that stays inside a base directory, a DOM parser that refuses a DOCTYPE, and a serialization
 * filter set before readObject.
 */
public class CommonVulnerabilities {

    // ---- SQL: values are bound, identifiers come from an allow-list ----

    /** Public sort keys mapped to SQL text this class wrote; no character of the request reaches the SQL. */
    static final Map<String, String> SORT_COLUMNS = Map.of(
            "name", "name",
            "price", "price",
            "newest", "added_on");

    static final Map<String, String> DIRECTIONS = Map.of("asc", "ASC", "desc", "DESC");

    /**
     * The text after ORDER BY for a sort key and direction taken from a request.
     *
     * @throws IllegalArgumentException for any key or direction the API doesn't document
     */
    public static String orderBy(String sort, String dir) {
        String column = sort == null ? null : SORT_COLUMNS.get(sort);
        String direction = dir == null ? null : DIRECTIONS.get(dir);
        if (column == null || direction == null) {
            throw new IllegalArgumentException("unsupported sort: " + sort + " " + dir);
        }
        return column + " " + direction;
    }

    /** The product list query for a request; its ORDER BY comes from the allow-list. */
    public static String productQuery(String sort, String dir) {
        return "SELECT name FROM product ORDER BY " + orderBy(sort, dir);
    }

    /** A value goes in a bind parameter (unit 7.4.5); only identifiers need the allow-list. */
    public List<String> findUserSecure(Connection connection, String username) throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT username FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) names.add(rs.getString(1));
            }
        }
        return names;
    }

    // ---- Paths: normalize, compare elements, then follow links ----

    /**
     * The file under {@code base} that a request for {@code name} may open. normalize() and
     * Path.startsWith refuse {@code ..} and a sibling directory whose name starts like the base;
     * toRealPath() refuses a symbolic link that leads outside it, and is what decides when the
     * text and the disk disagree.
     *
     * @throws SecurityException when the name leads outside the base
     * @throws java.nio.file.NoSuchFileException when the file doesn't exist
     */
    public static Path resolveInside(Path base, String name) throws IOException {
        Path normalized = base.resolve(name).normalize();
        if (!normalized.startsWith(base.normalize())) {
            throw new SecurityException("outside the base: " + name);
        }
        Path realBase = base.toRealPath();
        Path real = base.resolve(name).toRealPath();
        if (!real.startsWith(realBase)) {
            throw new SecurityException("outside the base through a link: " + name);
        }
        return real;
    }

    // ---- XML: refuse any DOCTYPE, so no entity can name a file or a URL ----

    /** A DOM factory for documents that never need a DOCTYPE. */
    public static DocumentBuilderFactory newSafeDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory;
    }

    /** The text of the first element named {@code tag}, parsed with the safe factory. */
    public static String textOf(InputStream xml, String tag) throws Exception {
        DocumentBuilder builder = newSafeDocumentBuilderFactory().newDocumentBuilder();
        builder.setErrorHandler(new DefaultHandler() {   // throw, without printing to stderr
            @Override
            public void fatalError(SAXParseException e) throws SAXParseException {
                throw e;
            }
        });
        return builder.parse(xml).getElementsByTagName(tag).item(0).getTextContent();
    }

    // ---- Deserialization: the filter decides each class before it is built ----

    /**
     * Reads an object, allowing only the given classes at any depth. Every other class is
     * rejected with InvalidClassException before its readObject runs.
     */
    public static Object readAllowing(byte[] data, Set<Class<?>> allowed) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            in.setObjectInputFilter(ObjectInputFilter.allowFilter(allowed::contains, ObjectInputFilter.Status.REJECTED));
            return in.readObject();
        }
    }
}
