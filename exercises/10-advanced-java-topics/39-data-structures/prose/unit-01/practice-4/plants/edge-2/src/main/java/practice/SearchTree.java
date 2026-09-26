package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public final class SearchTree<T extends Comparable<T>> {

    private static final class Node<T> {
        final T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> root;

    /** Inserts a value; a value already present is ignored; null is refused. */
    public void insert(T value) {
        Objects.requireNonNull(value, "value");
        root = insert(root, value);
    }

    private Node<T> insert(Node<T> node, T value) {
        if (node == null) {
            return new Node<>(value);
        }
        int cmp = value.compareTo(node.value);
        if (cmp < 0) {
            node.left = insert(node.left, value);
        } else if (cmp > 0) {
            node.right = insert(node.right, value);
        }
        return node;
    }

    /** Left subtree, node, right subtree. */
    public List<T> inOrder() {
        List<T> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    private void inOrder(Node<T> node, List<T> out) {
        if (node != null) {
            inOrder(node.left, out);
            out.add(node.value);
            inOrder(node.right, out);
        }
    }

    /** Node, left subtree, right subtree. */
    public List<T> preOrder() {
        List<T> out = new ArrayList<>();
        preOrder(root, out);
        return out;
    }

    private void preOrder(Node<T> node, List<T> out) {
        if (node != null) {
            out.add(node.value);
            preOrder(node.left, out);
            preOrder(node.right, out);
        }
    }

    /** Left subtree, right subtree, node. */
    public List<T> postOrder() {
        List<T> out = new ArrayList<>();
        postOrder(root, out);
        return out;
    }

    private void postOrder(Node<T> node, List<T> out) {
        if (node != null) {
            postOrder(node.left, out);
            postOrder(node.right, out);
            out.add(node.value);
        }
    }

    /** Breadth-first, one level at a time, left to right. */
    public List<T> levelOrder() {
        List<T> out = new ArrayList<>();
        if (root == null) {
            return out;
        }
        out.add(root.value);
        children(root, out);
        return out;
    }

    // adds a node's children, then recurses into them one subtree at a time
    private void children(Node<T> node, List<T> out) {
        if (node.left != null) {
            out.add(node.left.value);
        }
        if (node.right != null) {
            out.add(node.right.value);
        }
        if (node.left != null) {
            children(node.left, out);
        }
        if (node.right != null) {
            children(node.right, out);
        }
    }

    /** Edges on the longest root-to-leaf path; -1 for an empty tree. */
    public int height() {
        return height(root);
    }

    private int height(Node<T> node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(height(node.left), height(node.right));
    }
}
