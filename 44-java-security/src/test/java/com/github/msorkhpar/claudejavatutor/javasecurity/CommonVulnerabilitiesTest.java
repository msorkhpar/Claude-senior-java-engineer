package com.github.msorkhpar.claudejavatutor.javasecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Common Vulnerabilities Tests")
class CommonVulnerabilitiesTest {

    @Nested
    @DisplayName("ORDER BY allow-list")
    class OrderByTest {

        @Test
        @DisplayName("Maps documented keys to the SQL text the class wrote")
        void documentedKeys() {
            assertThat(CommonVulnerabilities.orderBy("price", "asc")).isEqualTo("price ASC");
            assertThat(CommonVulnerabilities.orderBy("newest", "desc")).isEqualTo("added_on DESC");
            assertThat(CommonVulnerabilities.productQuery("name", "desc"))
                    .isEqualTo("SELECT name FROM product ORDER BY name DESC");
        }

        @Test
        @DisplayName("Refuses a real but undocumented column and anything that isn't a key")
        void refusesEverythingElse() {
            for (String sort : new String[]{"cost", "added_on", "PRICE", "price, cost",
                    "CASE WHEN 1=1 THEN price ELSE -price END", "price; DROP TABLE staff", null}) {
                assertThatThrownBy(() -> CommonVulnerabilities.orderBy(sort, "asc"))
                        .as("sort %s", sort)
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        @DisplayName("Refuses a direction with anything beyond asc or desc")
        void refusesDirection() {
            assertThatThrownBy(() -> CommonVulnerabilities.orderBy("name", "asc, cost"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> CommonVulnerabilities.orderBy("name", null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Path inside a base directory")
    class PathTest {

        @TempDir
        Path tmp;

        private Path root;
        private Path base;

        /* root/uploads/invoice.txt, root/uploads/2026/, root/uploads/team -> ../config, root/config/db.properties,
           root/uploads-private/payroll.txt, root/secret.txt */
        @BeforeEach
        void layOut() throws IOException {
            root = tmp.toRealPath();
            base = Files.createDirectories(root.resolve("uploads"));
            Files.writeString(base.resolve("invoice.txt"), "invoice");
            Files.createDirectories(base.resolve("2026"));
            Files.createDirectories(root.resolve("config"));
            Files.writeString(root.resolve("config/db.properties"), "password=placeholder");
            Files.createDirectories(root.resolve("uploads-private"));
            Files.writeString(root.resolve("uploads-private/payroll.txt"), "payroll");
            Files.writeString(root.resolve("secret.txt"), "secret");
            Files.createSymbolicLink(base.resolve("team"), Path.of("../config"));
        }

        @Test
        @DisplayName("Returns a file of the base, also through a .. that stays inside")
        void insideFiles() throws IOException {
            assertThat(Files.readString(CommonVulnerabilities.resolveInside(base, "invoice.txt"))).isEqualTo("invoice");
            assertThat(Files.readString(CommonVulnerabilities.resolveInside(base, "2026/../invoice.txt")))
                    .isEqualTo("invoice");
        }

        @Test
        @DisplayName("Refuses .. above the base and a sibling whose name starts like the base")
        void refusesTraversal() {
            assertThatThrownBy(() -> CommonVulnerabilities.resolveInside(base, "../config/db.properties"))
                    .isInstanceOf(SecurityException.class);
            assertThatThrownBy(() -> CommonVulnerabilities.resolveInside(base, "../uploads-private/payroll.txt"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Refuses a symbolic link that leads outside, including link/..")
        void refusesLinks() {
            assertThatThrownBy(() -> CommonVulnerabilities.resolveInside(base, "team/db.properties"))
                    .isInstanceOf(SecurityException.class);
            // normalizes to uploads/secret.txt, but the file system opens root/secret.txt
            assertThatThrownBy(() -> CommonVulnerabilities.resolveInside(base, "team/../secret.txt"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("Serves the base's files when the base is reached through a link")
        void baseThroughLink() throws IOException {
            Path linked = Files.createSymbolicLink(root.resolve("current"), Path.of("uploads"));
            assertThat(Files.readString(CommonVulnerabilities.resolveInside(linked, "invoice.txt"))).isEqualTo("invoice");
        }

        @Test
        @DisplayName("A missing file is NoSuchFileException, not a pass")
        void missingFile() {
            assertThatThrownBy(() -> CommonVulnerabilities.resolveInside(base, "missing.txt"))
                    .isInstanceOf(NoSuchFileException.class);
        }
    }

    @Nested
    @DisplayName("XML without a DOCTYPE")
    class XmlTest {

        private static ByteArrayInputStream xml(String text) {
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("Reads a plain document")
        void plainDocument() throws Exception {
            assertThat(CommonVulnerabilities.textOf(xml("<order><note>leave at the door</note></order>"), "note"))
                    .isEqualTo("leave at the door");
        }

        @Test
        @DisplayName("Refuses a document that declares an external entity, without reading the file")
        void refusesExternalEntity(@TempDir Path dir) throws IOException {
            Files.writeString(dir.resolve("secret.txt"), "contents of secret.txt");
            Path doc = Files.writeString(dir.resolve("order.xml"), """
                    <?xml version="1.0"?>
                    <!DOCTYPE order [<!ENTITY note SYSTEM "secret.txt">]>
                    <order><note>&note;</note></order>
                    """);

            assertThatThrownBy(() -> {
                try (var in = Files.newInputStream(doc)) {
                    CommonVulnerabilities.textOf(in, "note");
                }
            }).isInstanceOf(SAXParseException.class).hasMessageContaining("DOCTYPE is disallowed");
        }

        @Test
        @DisplayName("Refuses even a DOCTYPE with only internal entities")
        void refusesAnyDoctype() {
            assertThatThrownBy(() -> CommonVulnerabilities.textOf(
                    xml("<!DOCTYPE order [<!ENTITY n \"x\">]><order><note>&n;</note></order>"), "note"))
                    .isInstanceOf(SAXParseException.class);
        }
    }

    @Nested
    @DisplayName("Serialization filter")
    class FilterTest {

        record Preferences(String theme, int pageSize) implements Serializable {}

        record Envelope(String kind, Object payload) implements Serializable {}

        static final AtomicInteger REBUILT = new AtomicInteger();

        /** Harmless, but not a class the reader expects; counts each time it is rebuilt. */
        static final class Housekeeping implements Serializable {
            private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
                in.defaultReadObject();
                REBUILT.incrementAndGet();
            }
        }

        @BeforeEach
        void reset() {
            REBUILT.set(0);
        }

        private static byte[] bytes(Object o) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(o);
            }
            return out.toByteArray();
        }

        @Test
        @DisplayName("Reads an allowed class")
        void allowed() throws Exception {
            Object read = CommonVulnerabilities.readAllowing(bytes(new Preferences("dark", 50)), Set.of(Preferences.class));
            assertThat(read).isEqualTo(new Preferences("dark", 50));
        }

        @Test
        @DisplayName("Rejects another class before its readObject runs")
        void rejectsTopLevel() throws IOException {
            byte[] data = bytes(new Housekeeping());
            assertThatThrownBy(() -> CommonVulnerabilities.readAllowing(data, Set.of(Preferences.class)))
                    .isInstanceOf(InvalidClassException.class);
            assertThat(REBUILT.get()).isZero();
        }

        @Test
        @DisplayName("Rejects another class nested inside an allowed one")
        void rejectsNested() throws IOException {
            byte[] data = bytes(new Envelope("prefs", new Housekeeping()));
            assertThatThrownBy(() -> CommonVulnerabilities.readAllowing(data, Set.of(Envelope.class, Preferences.class)))
                    .isInstanceOf(InvalidClassException.class);
            assertThat(REBUILT.get()).isZero();
        }

        @Test
        @DisplayName("Without a filter the class's readObject runs before any cast")
        void unfilteredRuns() throws Exception {
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes(new Housekeeping())))) {
                Object read = in.readObject();
                assertThat(read).isNotInstanceOf(Preferences.class);
            }
            assertThat(REBUILT.get()).isEqualTo(1);
        }
    }
}
