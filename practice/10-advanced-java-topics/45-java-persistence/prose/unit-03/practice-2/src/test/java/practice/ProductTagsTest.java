package practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTagsTest {

    private String url;
    /** Auto-commit on: sees only what was committed, and plays the other writers. */
    private Connection observer;
    private final List<Connection> opened = new ArrayList<>();

    @BeforeEach
    void openDatabase() throws SQLException {
        url = "jdbc:h2:mem:" + UUID.randomUUID();
        observer = DriverManager.getConnection(url);
        try (Statement s = observer.createStatement()) {
            s.execute("CREATE TABLE product (id INT PRIMARY KEY, tags VARCHAR(500) NOT NULL, version INT NOT NULL)");
            s.execute("INSERT INTO product VALUES (5, 'steel', 0), (6, 'oak', 3), (7, 'linen', 0), (8, 'wool', 11)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        for (Connection c : opened) c.close();
        observer.close();
    }

    private Connection editor() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        opened.add(c);
        try (Statement s = c.createStatement()) {
            s.execute("SET LOCK_TIMEOUT 5000");
        }
        return c;
    }

    private String tagsOf(int productId) throws SQLException {
        try (PreparedStatement p = observer.prepareStatement("SELECT tags FROM product WHERE id = ?")) {
            p.setInt(1, productId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getString(1);
            }
        }
    }

    /** Another writer that keeps the version column: appends a tag and moves the version on. */
    private void otherJobAppends(int productId, String tag) throws SQLException {
        try (PreparedStatement p = observer.prepareStatement(
                "UPDATE product SET tags = tags || ',' || ?, version = version + 1 WHERE id = ?")) {
            p.setString(1, tag);
            p.setInt(2, productId);
            p.executeUpdate();
        }
    }

    private static <T> T inBackground(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        Thread t = new Thread(task, "editor");
        t.setDaemon(true);
        t.start();
        return task.get(15, TimeUnit.SECONDS);
    }

    @Test
    void addsTagsOneAfterAnother() throws Exception {
        ProductTags tags = new ProductTags();
        Connection kim = editor();

        assertThat(inBackground(() -> tags.addTag(kim, 5, "blue"))).isTrue();
        assertThat(inBackground(() -> tags.addTag(kim, 5, "sale"))).isTrue();

        assertThat(tagsOf(5)).isEqualTo("steel,blue,sale");
    }

    @Test
    void aTagAddedByAnotherJobMeanwhileIsKept() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        ProductTags tags = new ProductTags(() -> {
            if (reads.incrementAndGet() == 1) otherJobAppends(6, "outdoor");
        });
        Connection lea = editor();

        inBackground(() -> tags.addTag(lea, 6, "sale"));

        assertThat(reads.get()).as("reads through load(); the other writers run from there").isPositive();
        assertThat(tagsOf(6)).isEqualTo("oak,outdoor,sale");
    }

    @Test
    void twoEditorsAddingAtOnceBothKeepTheirTag() throws Exception {
        Connection ivy = editor(), jon = editor();
        AtomicInteger reads = new AtomicInteger();
        // Jon adds his tag while Ivy is between her read and her save.
        ProductTags ivysTags = new ProductTags(() -> {
            if (reads.incrementAndGet() == 1) inBackground(() -> new ProductTags().addTag(jon, 7, "eco"));
        });

        inBackground(() -> ivysTags.addTag(ivy, 7, "gift"));

        assertThat(reads.get()).as("reads through load(); the other writers run from there").isPositive();
        assertThat(tagsOf(7)).isEqualTo("linen,eco,gift");
    }

    @Test
    void anEditorWhoLosesTwiceStillSavesOnTheThirdTry() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        ProductTags tags = new ProductTags(() -> {
            int n = reads.incrementAndGet();
            if (n == 1) otherJobAppends(8, "warm");
            if (n == 2) otherJobAppends(8, "winter");
        });
        Connection max = editor();

        boolean saved = inBackground(() -> tags.addTag(max, 8, "sale"));

        assertThat(reads.get()).as("reads through load(); the other writers run from there").isPositive();
        assertThat(saved).isTrue();
        assertThat(tagsOf(8)).isEqualTo("wool,warm,winter,sale");
    }
}
