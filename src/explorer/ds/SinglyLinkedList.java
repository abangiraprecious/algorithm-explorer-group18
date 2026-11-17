package explorer.ds;

/**
 * Singly linked list with head and tail pointers (replaces java.util.LinkedList).
 * Every node holds a value and ONE reference: next.
 *
 * addFirst / addLast / removeFirst                       O(1)
 * removeLast                                             O(n)  – must walk to the node before the tail
 * insertBefore / insertAfter / removeBefore / removeAfter O(n)  – must first find the given element
 * insertAt / removeValue / indexOf / get                 O(n)  – must walk from the head
 * reverse                                                O(n)  – re-points every next reference
 */
public class SinglyLinkedList {

    public static final class Node {
        public int value;
        public Node next;
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
        head = n;
        if (tail == null) tail = n;
        size++;
        ops += 2;
        lastIndex = 0;
    }

    /** Inserts a new node at the end, using the tail pointer. O(1). */
    public void addLast(int v) {
        Node n = new Node(v);
        if (tail == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
        ops += 2;
        lastIndex = size - 1;
    }

    /** Inserts v at position index (0 = head). O(n). */
    public void insertAt(int index, int v) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("position " + index + " is outside 0.." + size);
        if (index == 0) { addFirst(v); return; }
        if (index == size) { addLast(v); return; }
        Node prev = head;
        for (int i = 0; i < index - 1; i++) { prev = prev.next; ops++; }
        Node n = new Node(v);
        n.next = prev.next;
        prev.next = n;
        size++;
        ops += 2;
        lastIndex = index;
    }

    /** Inserts v just before the first node holding x. O(n): walks to the node before x. */
    public void insertBefore(int x, int v) {
        if (head == null) throw new IllegalStateException("The list is empty, so " + x + " is not in it");
        ops++;
        if (head.value == x) { addFirst(v); return; }
        Node prev = head;
        int i = 1;
        while (prev.next != null && prev.next.value != x) { prev = prev.next; ops++; i++; }
        if (prev.next == null) throw new IllegalStateException(x + " is not in the list, so nothing was inserted");
        Node n = new Node(v);
        n.next = prev.next;
        prev.next = n;
        size++;
        ops += 2;
        lastIndex = i;
    }

    /** Inserts v just after the first node holding x. O(n) to find x, then O(1) to link. */
    public void insertAfter(int x, int v) {
        Node cur = head;
        int i = 0;
        while (cur != null && cur.value != x) { cur = cur.next; ops++; i++; }
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was inserted");
        Node n = new Node(v);
        n.next = cur.next;
        cur.next = n;
        if (cur == tail) tail = n;
        size++;
        ops += 2;
        lastIndex = i + 1;
    }

    /** Deletes the first node. O(1). */
    public int removeFirst() {
        if (head == null) throw new IllegalStateException("The list is empty, so there is nothing to delete");
        int v = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        ops++;
        lastIndex = -1;
        return v;
    }

    /** Deletes the last node. O(n): a singly linked node cannot see its predecessor, so we walk to it. */
    public int removeLast() {
        if (head == null) throw new IllegalStateException("The list is empty, so there is nothing to delete");
        if (head == tail) return removeFirst();
        Node prev = head;
        while (prev.next != tail) { prev = prev.next; ops++; }
        int v = tail.value;
        prev.next = null;
        tail = prev;
        size--;
        ops += 2;
        lastIndex = -1;
        return v;
    }

    /** Deletes the node just before the first node holding x. O(n). Returns the deleted value. */
    public int removeBefore(int x) {
        if (head == null) throw new IllegalStateException("The list is empty, so " + x + " is not in it");
        ops++;
        if (head.value == x) throw new IllegalStateException(x + " is the first node, so there is nothing before it to delete");
        if (head.next != null && head.next.value == x) { ops++; return removeFirst(); }
        // keep two pointers: before2 -> before -> cur, and stop when cur holds x
        Node before2 = head, before = head.next;
        Node cur = before == null ? null : before.next;
        while (cur != null && cur.value != x) { before2 = before; before = cur; cur = cur.next; ops++; }
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was deleted");
        before2.next = cur;
        size--;
        ops++;
        lastIndex = -1;
        return before.value;
    }

    /** Deletes the node just after the first node holding x. O(n) to find x, then O(1) to unlink. */
    public int removeAfter(int x) {
        Node cur = head;
        while (cur != null && cur.value != x) { cur = cur.next; ops++; }
        if (cur == null) throw new IllegalStateException(x + " is not in the list, so nothing was deleted");
        if (cur.next == null) throw new IllegalStateException(x + " is the last node, so there is nothing after it to delete");
        Node gone = cur.next;
        cur.next = gone.next;
        if (gone == tail) tail = cur;
        size--;
        ops++;
        lastIndex = -1;
        return gone.value;
    }

    /** Removes the first node holding v. Returns its former index or -1. O(n). */
    public int removeValue(int v) {
        Node prev = null, cur = head;
        int i = 0;
        while (cur != null) {
            ops++;
            if (cur.value == v) {
                if (prev == null) head = cur.next; else prev.next = cur.next;
                if (cur == tail) tail = prev;
                size--;
                lastIndex = -1;
                return i;
            }
            prev = cur;
            cur = cur.next;
            i++;
        }
        lastIndex = -1;
        return -1;
    }

    /** Search: position of the first node holding v, or -1. O(n). */
    public int indexOf(int v) {
        Node cur = head;
        int i = 0;
        while (cur != null) {
            ops++;
            if (cur.value == v) { lastIndex = i; return i; }
            cur = cur.next;
            i++;
        }
        lastIndex = -1;
        return -1;
    }

    /** Value at position index. O(n): there is no random access. */
    public int get(int index) {
        if (size == 0) throw new IllegalStateException("the list is empty");
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("position " + index + " is outside 0.." + (size - 1));
        Node cur = head;
        for (int i = 0; i < index; i++) { cur = cur.next; ops++; }
        ops++;
        lastIndex = index;
        return cur.value;
    }

    /** Reverses the list in place by re-pointing every next reference. O(n). */
    public void reverse() {
        Node prev = null, cur = head;
        tail = head;
        while (cur != null) {
            Node next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
            ops++;
        }
        head = prev;
        lastIndex = -1;
    }

    /** Display forward: the values from head to tail. O(n). */
    public String display() {
        StringBuilder sb = new StringBuilder("head");
        for (Node n = head; n != null; n = n.next) { sb.append(" → ").append(n.value); ops++; }
        return sb.append(" → null").toString();
    }

    public Node head() { return head; }
    public int size() { return size; }
}
