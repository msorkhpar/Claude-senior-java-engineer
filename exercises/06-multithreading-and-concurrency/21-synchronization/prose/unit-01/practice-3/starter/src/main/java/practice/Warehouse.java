package practice;

/**
 * Moves stock between bins and counts pairs of bins. Each bin's quantity is guarded by the bin's
 * own monitor; an operation on two bins holds both. Rebalancing jobs, pickers and auditors run
 * these from many threads at once.
 */
public class Warehouse {

    /** A storage bin. Its code, such as "A-04-2", is unique in the warehouse and never changes. */
    public static final class Bin {
        private final String code;
        private int quantity;

        public Bin(String code, int quantity) {
            this.code = code;
            this.quantity = quantity;
        }

        public String code() {
            return code;
        }

        public int quantity() {
            synchronized (this) {
                return quantity;
            }
        }
    }

    /** Told each time an operation holds its first bin and is about to take the second. */
    public interface Probe {
        void holdingFirst(Bin first);
    }

    private final Probe probe;

    public Warehouse(Probe probe) {
        this.probe = probe;
    }

    public Warehouse() {
        this(first -> { });
    }

    /** Runs action while holding the monitors of both bins: first's, then second's. */
    private void holdingBoth(Bin first, Bin second, Runnable action) {
        synchronized (first) {
            probe.holdingFirst(first);
            synchronized (second) {
                action.run();
            }
        }
    }

    /** Moves qty units from one bin to another. False, and nothing moves, if from holds fewer. */
    public boolean move(Bin from, Bin to, int qty) {
        if (from == to) return false;
        boolean[] moved = new boolean[1];
        Runnable action = () -> {
            if (from.quantity < qty) return;
            from.quantity -= qty;
            to.quantity += qty;
            moved[0] = true;
        };
        // TODO: Run action while holding both bins (holdingBoth), then return moved[0]. Pickers and
        //       rebalancing jobs move stock in both directions between the same bins at the same
        //       time, while auditors count them, and none of them may ever hang.
        throw new IllegalStateException("TODO: move");
    }

    /** The units in two bins together, as one consistent reading of both. */
    public int count(Bin a, Bin b) {
        if (a == b) return a.quantity();
        int[] total = new int[1];
        Runnable action = () -> total[0] = a.quantity + b.quantity;
        // TODO: Run action while holding both bins (holdingBoth), then return total[0]. Auditors
        //       count bins while moves between them are under way, and nobody may ever hang.
        throw new IllegalStateException("TODO: count");
    }
}
