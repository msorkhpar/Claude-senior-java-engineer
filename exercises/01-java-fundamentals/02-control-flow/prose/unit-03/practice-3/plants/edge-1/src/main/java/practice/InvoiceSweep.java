package practice;

import java.util.function.IntConsumer;

/**
 * The nightly reconciliation sweeps invoice ids shard by shard. Invoice ids are positive ints, and
 * each shard owns an inclusive range of them; the last shard's range ends at the largest int.
 */
public class InvoiceSweep {

    /** An inclusive range of invoice ids. A range whose first id is above its last is empty. */
    public record Shard(int firstId, int lastId) {}

    /** Reconciles one shard and returns how many invoice ids it checked. */
    public static long sweep(Shard shard, IntConsumer reconcile) {
        long[] checked = new long[1];
        forEachId(shard.firstId(), shard.lastId(), id -> {
            reconcile.accept(id);
            checked[0]++;
        });
        return checked[0];
    }

    /** Hands every id from first to last, both included, to visit, in increasing order. */
    static void forEachId(int first, int last, IntConsumer visit) {
        int id = first;
        while (id <= last) {
            visit.accept(id);
            id++;
        }
    }
}
