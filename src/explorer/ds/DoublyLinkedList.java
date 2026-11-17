package explorer.ds;

/**
 * Doubly linked list with head and tail pointers.
 * Every node holds a value and TWO references: prev and next, so the list can be walked
 * in both directions and a node can reach its predecessor in O(1).
 *
 * addFirst / addLast / removeFirst / removeLast           O(1)  – removeLast is O(1) here thanks to tail.prev
 * insertBefore / insertAfter / removeBefore / removeAfter O(n)  – must first find the given element
 * removeValue / indexOf                                   O(n)
 * display forward / display backward                      O(n)
 */
public class DoublyLinkedList {

    public static final class Node {
        public int value;
        public Node prev, next;
        Node(int value) { this.value = value; }
    }

    private Node head, tail;
    private int size;
    public long ops;
    /** Index touched by the last operation (for highlighting), -1 if none. */
    public int lastIndex = -1;

    /** Inserts a new node at the beginning. O(1). */
    public void addFirst(int v) {
        Node n = new Node(v);
        n.next = head;
        if (head != null) head.prev = n; else tail = n;
        head = n;
        size++;
        ops += 3;
        lastIndex = 0;
    }

    /** Inserts a new node at the end. O(1). */
    public void addLast(int v) {
        Node n = new Node(v);
        n.prev = tail;
        if (tail != null) tail.next = n; else head = n;
        tail = n;
        size++;
        ops += 3;
        lastIndex = size - 1;
    }

    /** Finds the first node holding x and its position (stored in lastIndex), or null. O(n). */
    private Node find(int x) {
        Node cur = head;
        int i = 0;
        while (cur != null && cur.value != x) { cur = cur.next; ops++; i++; }
        if (cur != null) ops++;
        lastIndex = cur == null ? -1 : i;
        return cur;
    }

    /** Inserts v just before the first node holding x. O(n) to find x, then O(1) using x.prev. */
    public void insertBefore(int x, int v) {
        if (head == null) throw new IllegalStateException("The list is empty, so " + x + " is not in it");
        Node cur = find(x);
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was inserted");
        if (cur == head) { addFirst(v); return; }
        int at = lastIndex;
        Node n = new Node(v);
        n.prev = cur.prev;
        n.next = cur;
        cur.prev.next = n;
        cur.prev = n;
        size++;
        ops += 4;
        lastIndex = at;
    }

    /** Inserts v just after the first node holding x. O(n) to find x, then O(1). */
    public void insertAfter(int x, int v) {
        Node cur = find(x);
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was inserted");
        if (cur == tail) { addLast(v); return; }
        int at = lastIndex + 1;
        Node n = new Node(v);
        n.prev = cur;
        n.next = cur.next;
        cur.next.prev = n;
        cur.next = n;
        size++;
        ops += 4;
        lastIndex = at;
    }

    /** Unlinks node z by joining its neighbours to each other. O(1). */
    private int unlink(Node z) {
        if (z.prev != null) z.prev.next = z.next; else head = z.next;
        if (z.next != null) z.next.prev = z.prev; else tail = z.prev;
        size--;
        ops += 2;
        lastIndex = -1;
        return z.value;
    }

    /** Deletes the first node. O(1). */
    public int removeFirst() {
        if (head == null) throw new IllegalStateException("The list is empty, so there is nothing to delete");
        return unlink(head);
    }

    /** Deletes the last node. O(1): tail.prev gives the new tail directly. */
    public int removeLast() {
        if (tail == null) throw new IllegalStateException("The list is empty, so there is nothing to delete");
        return unlink(tail);
    }

    /** Deletes the node just before the first node holding x. O(n). */
    public int removeBefore(int x) {
        if (head == null) throw new IllegalStateException("The list is empty, so " + x + " is not in it");
        Node cur = find(x);
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was deleted");
        if (cur.prev == null) throw new IllegalStateException(x + " is the first node, so there is nothing before it to delete");
        return unlink(cur.prev);
    }

    /** Deletes the node just after the first node holding x. O(n). */
    public int removeAfter(int x) {
        Node cur = find(x);
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was deleted");
        if (cur.next == null) throw new IllegalStateException(x + " is the last node, so there is nothing after it to delete");
        return unlink(cur.next);
    }

    /** Deletes the first node holding x. Returns its former index or -1. O(n). */
    public int removeValue(int x) {
        Node cur = find(x);
        if (cur == null) return -1;
        int at = lastIndex;
        unlink(cur);
        return at;
    }

    /** Search: position of the first node holding x, or -1. O(n). */
    public int indexOf(int x) {
        find(x);
        return lastIndex;
    }

    /** Display forward, following next from the head. O(n). */
    public String displayForward() {
        StringBuilder sb = new StringBuilder("head");
        for (Node n = head; n != null; n = n.next) { sb.append(" → ").append(n.value); ops++; }
        return sb.append(" → null").toString();
    }

    /** Display backward, following prev from the tail. O(n). Only possible because of the prev links. */
    public String displayBackward() {
        StringBuilder sb = new StringBuilder("tail");
        for (Node n = tail; n != null; n = n.prev) { sb.append(" → ").append(n.value); ops++; }
        return sb.append(" → null").toString();
    }

    public Node head() { return head; }
    public Node tail() { return tail; }
    public int size() { return size; }
}
