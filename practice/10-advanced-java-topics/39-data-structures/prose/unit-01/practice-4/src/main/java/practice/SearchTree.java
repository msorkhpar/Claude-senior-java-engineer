package practice;

import java.util.List;

public final class SearchTree<T extends Comparable<T>> {

    /** Inserts a value; a value already present is ignored; null is refused. */
    public void insert(T value) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Left subtree, node, right subtree. */
    public List<T> inOrder() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Node, left subtree, right subtree. */
    public List<T> preOrder() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Left subtree, right subtree, node. */
    public List<T> postOrder() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Breadth-first, one level at a time, left to right. */
    public List<T> levelOrder() {
        throw new UnsupportedOperationException("TODO");
    }

    /** Edges on the longest root-to-leaf path; -1 for an empty tree. */
    public int height() {
        throw new UnsupportedOperationException("TODO");
    }
}
