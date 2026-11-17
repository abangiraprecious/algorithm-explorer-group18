package explorer.ds;

/**
 * Binary search tree of ints (no duplicates).
 *
 * insert / search / delete   O(h): O(log n) when balanced, O(n) when the tree degenerates
 * traversals                 O(n), written recursively
 * height                     O(n), recursive
 */
public class BinarySearchTree {

    public static final class Node {
        public int key;
        public Node left, right;
        Node(int key) { this.key = key; }
    }

    private Node root;
    private int size;
    public long ops;
    /** Keys visited by the most recent insert/search/delete – drawn as the path. */
    private IntList lastPath = new IntList();
    private IntList path = new IntList();

    public Node root() { return root; }
    public int size() { return size; }

    /** @return false if the key was already present. */
    public boolean insert(int key) {
        path = new IntList();
        boolean[] added = new boolean[1];
        root = insert(root, key, added);
        copyPath();
        if (added[0]) size++;
        return added[0];
    }

    private Node insert(Node node, int key, boolean[] added) {
        if (node == null) { ops++; added[0] = true; path.add(key); return new Node(key); }
        ops++; // comparison
        path.add(node.key);
        if (key < node.key) node.left = insert(node.left, key, added);
        else if (key > node.key) node.right = insert(node.right, key, added);
        return node;
    }

    public boolean search(int key) {
        path = new IntList();
        Node cur = root;
        boolean found = false;
        while (cur != null) {
            ops++;
            path.add(cur.key);
            if (key == cur.key) { found = true; break; }
            cur = key < cur.key ? cur.left : cur.right;
        }
        copyPath();
        return found;
    }

    /** @return a description of which deletion case was used, or null if not found. */
    public String delete(int key) {
        path = new IntList();
        String[] how = new String[1];
        root = delete(root, key, how);
        copyPath();
        if (how[0] != null) size--;
        return how[0];
    }

    private Node delete(Node node, int key, String[] how) {
        if (node == null) return null;
        ops++;
        path.add(node.key);
        if (key < node.key) { node.left = delete(node.left, key, how); return node; }
        if (key > node.key) { node.right = delete(node.right, key, how); return node; }
        // found the node
        if (node.left == null && node.right == null) { how[0] = "leaf node – simply removed"; return null; }
        if (node.left == null) { how[0] = "one child – replaced by its right child"; return node.right; }
        if (node.right == null) { how[0] = "one child – replaced by its left child"; return node.left; }
        // two children: copy the in-order successor (smallest key in the right subtree)
        Node succ = node.right;
        while (succ.left != null) { succ = succ.left; ops++; }
        how[0] = "two children – replaced by in-order successor " + succ.key;
        node.key = succ.key;
        String[] ignore = new String[1];
        node.right = delete(node.right, succ.key, ignore);
        return node;
    }

    public void inorder(Node n, IntList out) {
        if (n == null) return;
        inorder(n.left, out); out.add(n.key); ops++; inorder(n.right, out);
    }

    public void preorder(Node n, IntList out) {
        if (n == null) return;
        out.add(n.key); ops++; preorder(n.left, out); preorder(n.right, out);
    }

    public void postorder(Node n, IntList out) {
        if (n == null) return;
        postorder(n.left, out); postorder(n.right, out); out.add(n.key); ops++;
    }

    /** Height in edges; an empty tree has height -1. */
    public int height(Node n) {
        if (n == null) return -1;
        ops++;
        int l = height(n.left), r = height(n.right);
        return 1 + (l > r ? l : r);
    }

    public int min() {
        if (root == null) throw new IllegalStateException("the tree is empty");
        path = new IntList();
        Node c = root;
        path.add(c.key);
        while (c.left != null) { c = c.left; path.add(c.key); ops++; }
        copyPath();
        return c.key;
    }

    public int max() {
        if (root == null) throw new IllegalStateException("the tree is empty");
        path = new IntList();
        Node c = root;
        path.add(c.key);
        while (c.right != null) { c = c.right; path.add(c.key); ops++; }
        copyPath();
        return c.key;
    }

    public void clearPath() { path = new IntList(); copyPath(); }

    private void copyPath() { lastPath = path; }

    /** Path of keys visited by the most recent operation. */
    public IntList path() { return lastPath; }
}
