package practice;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public final class TotalsAction extends RecursiveAction {

    /** A tree node; TotalsAction fills in its total. */
    public static final class Node {
        private final long size;
        private final List<Node> children;
        private long total;

        public Node(long size, List<Node> children) {
            this.size = size;
            this.children = List.copyOf(children);
        }

        public long size() {
            return size;
        }

        public List<Node> children() {
            return children;
        }

        public long total() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }

    private final Node node;

    public TotalsAction(Node node) {
        this.node = node;
    }

    @Override
    protected void compute() {
        List<TotalsAction> children = node.children().stream().map(TotalsAction::new).toList();
        invokeAll(children);
        long total = node.size();
        for (Node child : node.children()) {
            total += child.total();
        }
        node.setTotal(total);
    }
}
