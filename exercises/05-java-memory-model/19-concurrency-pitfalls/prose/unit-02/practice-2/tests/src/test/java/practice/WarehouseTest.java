package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseTest {

    enum Point { AFTER_FIRST_READ, AFTER_FIRST_WRITE }

    /**
     * The shared stock map. When the mover thread is set, that thread is held right after its
     * first read of the map, or right after its first change to it, until the test releases it.
     * Other threads are never held.
     */
    static final class HoldingStock extends ConcurrentHashMap<String, Warehouse.Bins> {
        volatile Thread mover;
        volatile Point point;
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        private final AtomicBoolean first = new AtomicBoolean(true);

        private void after(Point p) {
            if (Thread.currentThread() == mover && point == p && first.compareAndSet(true, false)) {
                held.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override public Warehouse.Bins get(Object key) {
            Warehouse.Bins v = super.get(key); after(Point.AFTER_FIRST_READ); return v;
        }
        @Override public Warehouse.Bins put(String key, Warehouse.Bins value) {
            Warehouse.Bins v = super.put(key, value); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins putIfAbsent(String key, Warehouse.Bins value) {
            Warehouse.Bins v = super.putIfAbsent(key, value); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins replace(String key, Warehouse.Bins value) {
            Warehouse.Bins v = super.replace(key, value); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public boolean replace(String key, Warehouse.Bins oldValue, Warehouse.Bins newValue) {
            boolean v = super.replace(key, oldValue, newValue); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins compute(String key,
                BiFunction<? super String, ? super Warehouse.Bins, ? extends Warehouse.Bins> f) {
            Warehouse.Bins v = super.compute(key, f); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins computeIfPresent(String key,
                BiFunction<? super String, ? super Warehouse.Bins, ? extends Warehouse.Bins> f) {
            Warehouse.Bins v = super.computeIfPresent(key, f); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins computeIfAbsent(String key,
                Function<? super String, ? extends Warehouse.Bins> f) {
            Warehouse.Bins v = super.computeIfAbsent(key, f); after(Point.AFTER_FIRST_WRITE); return v;
        }
        @Override public Warehouse.Bins merge(String key, Warehouse.Bins value,
                BiFunction<? super Warehouse.Bins, ? super Warehouse.Bins, ? extends Warehouse.Bins> f) {
            Warehouse.Bins v = super.merge(key, value, f); after(Point.AFTER_FIRST_WRITE); return v;
        }
    }

    private final ExecutorService threads = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopThreads() {
        threads.shutdownNow();
    }

    private static Warehouse.Bins bins(Map<String, Integer> units) {
        return new Warehouse.Bins(units);
    }

    /** Starts the move on the mover thread; returns once the mover is held or has finished. */
    private Future<Boolean> startMove(HoldingStock stock, Point point, Warehouse w,
                                      String sku, String from, String to, int count) throws Exception {
        stock.point = point;
        Future<Boolean> move = threads.submit(() -> {
            stock.mover = Thread.currentThread();
            try {
                return w.move(sku, from, to, count);
            } finally {
                stock.held.countDown();                    // a move that was never held has finished
            }
        });
        assertThat(stock.held.await(5, TimeUnit.SECONDS)).as("move held or finished").isTrue();
        return move;
    }

    /** The order service ships units straight from the map, as it does in production. */
    private static boolean ship(HoldingStock stock, String sku, String bin, int count) {
        boolean[] shipped = {false};
        stock.compute(sku, (k, b) -> {
            if (b == null || b.in(bin) < count) return b;
            shipped[0] = true;
            return b.with(bin, b.in(bin) - count);
        });
        return shipped[0];
    }

    @Test
    void movesUnitsBetweenBinsAndRefusesWhatABinDoesNotHold() {
        ConcurrentHashMap<String, Warehouse.Bins> stock = new ConcurrentHashMap<>();
        stock.put("drill-18v", bins(Map.of("R2-07", 9, "R2-08", 1)));
        Warehouse w = new Warehouse(stock);

        assertThat(w.move("drill-18v", "R2-07", "R2-08", 4)).isTrue();
        assertThat(w.move("drill-18v", "R2-07", "R2-08", 6)).isFalse();
        assertThat(w.move("drill-18v", "R2-07", "Q1-01", 5)).isTrue();
        assertThat(w.move("ladder-3m", "R2-07", "R2-08", 1)).isFalse();

        assertThat(List.of(w.unitsIn("drill-18v", "R2-07"), w.unitsIn("drill-18v", "R2-08"),
                w.unitsIn("drill-18v", "Q1-01"))).containsExactly(0, 5, 5);
        assertThat(w.units("drill-18v")).isEqualTo(10);
        assertThat(w.units("ladder-3m")).isZero();
    }

    @Test
    void aStockCountDuringAMoveFindsEveryUnit() throws Exception {
        HoldingStock stock = new HoldingStock();
        stock.put("hose-25m", bins(Map.of("D4-02", 12, "D4-03", 3)));
        Warehouse w = new Warehouse(stock);

        Future<Boolean> move = startMove(stock, Point.AFTER_FIRST_WRITE, w, "hose-25m", "D4-02", "D4-03", 7);
        int counted = w.units("hose-25m");
        stock.release.countDown();

        assertThat(move.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(counted).isEqualTo(15);
        assertThat(w.unitsIn("hose-25m", "D4-03")).isEqualTo(10);
    }

    @Test
    void aMoveNeverTakesUnitsThatWereShippedMeanwhile() throws Exception {
        HoldingStock stock = new HoldingStock();
        stock.put("saw-blade", bins(Map.of("B1-11", 6)));
        Warehouse w = new Warehouse(stock);

        Future<Boolean> move = startMove(stock, Point.AFTER_FIRST_READ, w, "saw-blade", "B1-11", "B1-12", 6);
        ship(stock, "saw-blade", "B1-11", 6);
        stock.release.countDown();
        move.get(5, TimeUnit.SECONDS);

        assertThat(w.unitsIn("saw-blade", "B1-11")).isNotNegative();
        assertThat(w.unitsIn("saw-blade", "B1-12")).isNotNegative();
    }

    @Test
    void aMoveNeverUndoesAShipmentMadeMeanwhile() throws Exception {
        HoldingStock stock = new HoldingStock();
        stock.put("glue-1l", bins(Map.of("C3-05", 8, "C3-06", 0)));
        Warehouse w = new Warehouse(stock);

        Future<Boolean> move = startMove(stock, Point.AFTER_FIRST_READ, w, "glue-1l", "C3-05", "C3-06", 8);
        boolean shipped = ship(stock, "glue-1l", "C3-05", 3);
        stock.release.countDown();
        move.get(5, TimeUnit.SECONDS);

        assertThat(w.units("glue-1l")).isEqualTo(shipped ? 5 : 8);
    }
}
