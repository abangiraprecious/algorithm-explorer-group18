package explorer.ds;

/**
 * Singly linked list with head and tail pointers (replaces java.util.LinkedList).
 *
 * addFirst / addLast / removeFirst   O(1)
 * insertAt / removeValue / indexOf   O(n)  – must walk from the head
 * reverse                            O(n)  – re-points every next reference
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

    public void addFirst(int v) {
        Node n = new Node(v);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
        ops += 2;
        lastIndex = 0;
    }

    public void addLast(int v) {
        Node n = new Node(v);
        if (tail == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
        ops += 2;
        lastIndex = size - 1;
    }

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

    public int removeFirst() {
        if (head == null) throw new IllegalStateException("the list is empty");
        int v = head.value;
        head = head.next;
        if (head == null) tail = null;
        size--;
        ops++;
        lastIndex = -1;
        return v;
    }

    /** Removes the first node holding v. Returns its former index or -1. */
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

    public int get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("position " + index + " is outside 0.." + (size - 1));
        Node cur = head;
        for (int i = 0; i < index; i++) { cur = cur.next; ops++; }
        ops++;
        lastIndex = index;
        return cur.value;
    }

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

    public Node head() { return head; }
    public int size() { return size; }
}
