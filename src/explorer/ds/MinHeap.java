package explorer.ds;

/**
 * Binary min-heap stored in an array (replaces java.util.PriorityQueue).
 * For the item at index i: parent = (i-1)/2, left child = 2i+1, right child = 2i+2.
 *
 * insert      O(log n)  – sift up
 * extractMin  O(log n)  – move last to root, sift down
 * peek        O(1)
 * build       O(n)      – bottom-up heapify (Floyd's method)
 */
public class MinHeap {
    private int[] data = new int[8];
    private int size;
    public long ops;
    /** Indices swapped by the last operation, for highlighting. */
    public IntList touched = new IntList();

    /** Adds v at the end, then sifts it up while it is smaller than its parent. O(log n). */
    public void insert(int v) {
        touched = new IntList();
        if (size == data.length) grow();
        data[size] = v;
        int i = size++;
        ops++;
        touched.add(i);
        while (i > 0) {
            int p = (i - 1) / 2;
            ops++; // comparison
            if (data[i] >= data[p]) break;
            swap(i, p);
            i = p;
            touched.add(i);
        }
    }

    /** Removes the root (the minimum), moves the last item to the root and sifts it down. O(log n). */
    public int extractMin() {
        if (size == 0) throw new IllegalStateException("the heap is empty");
        touched = new IntList();
        int min = data[0];
        data[0] = data[--size];
        ops++;
        siftDown(0);
        return min;
    }

    /** The minimum, always at index 0. O(1). */
    public int peek() {
        if (size == 0) throw new IllegalStateException("the heap is empty");
        ops++;
        touched = new IntList();
        touched.add(0);
        return data[0];
    }

    /** Replaces the heap with the given values using bottom-up heapify. */
    public void build(int[] values) {
        touched = new IntList();
        data = new int[values.length < 8 ? 8 : values.length];
        for (int i = 0; i < values.length; i++) { data[i] = values[i]; ops++; }
        size = values.length;
        for (int i = size / 2 - 1; i >= 0; i--) siftDown(i);
    }

    /** Moves data[i] down while a child is smaller. O(log n). */
    private void siftDown(int i) {
        touched.add(i);
        while (true) {
            int l = 2 * i + 1, r = l + 1, smallest = i;
            if (l < size) { ops++; if (data[l] < data[smallest]) smallest = l; }
            if (r < size) { ops++; if (data[r] < data[smallest]) smallest = r; }
            if (smallest == i) return;
            swap(i, smallest);
            i = smallest;
            touched.add(i);
        }
    }

    /** Exchanges data[i] and data[j]. O(1). */
    private void swap(int i, int j) {
        int t = data[i]; data[i] = data[j]; data[j] = t;
        ops += 3;
    }

    /** Doubles the array when it is full. O(n), rare. */
    private void grow() {
        int[] bigger = new int[data.length * 2];
        for (int i = 0; i < size; i++) { bigger[i] = data[i]; ops++; }
        data = bigger;
    }

    public int get(int i) { return data[i]; }
    public int size() { return size; }
}
