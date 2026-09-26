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
        List<Node> kids = node.children().stream().limit(2).toList(); // a binary split's habit: two subtasks
        invokeAll(kids.stream().map(TotalsAction::new).toList());
        long total = node.size();
        for (Node child : kids) {
            total += child.total();
        }
        node.setTotal(total);
    }
}
