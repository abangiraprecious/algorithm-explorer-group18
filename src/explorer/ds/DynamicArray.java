package explorer.ds;

/**
 * A resizable array list written from scratch (replaces java.util.ArrayList).
 *
 * add          amortised O(1)  – doubles capacity when full, copying n items
 * get / set    O(1)
 * removeLast   O(1)
 * insertAt     O(n)            – shifts items right
 * removeAt     O(n)            – shifts items left
 */
public class DynamicArray<T> {
    private Object[] data;
    private int size;
    /** Number of element copies done by resizing (shows the amortised cost). */
    public long copies;

    public DynamicArray() { this(8); }

    public DynamicArray(int capacity) {
        data = new Object[capacity < 1 ? 1 : capacity];
    }

    /** Appends value; doubles the array first if it is full. Amortised O(1). */
    public void add(T value) {
        if (size == data.length) grow();
        data[size++] = value;
    }

    /** Inserts value at index by shifting later items right. O(n). */
    public void insertAt(int index, T value) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index " + index);
        if (size == data.length) grow();
        for (int i = size; i > index; i--) data[i] = data[i - 1];
        data[index] = value;
        size++;
    }

    /** Removes the item at index by shifting later items left. O(n). */
    @SuppressWarnings("unchecked")
    public T removeAt(int index) {
        check(index);
        T old = (T) data[index];
        for (int i = index; i < size - 1; i++) data[i] = data[i + 1];
        data[--size] = null;
        return old;
    }

    /** Removes the last item. O(1). */
    @SuppressWarnings("unchecked")
    public T removeLast() {
        if (size == 0) throw new IllegalStateException("array is empty");
        T old = (T) data[--size];
        data[size] = null;
        return old;
    }

    /** Item at index. O(1). */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        check(index);
        return (T) data[index];
    }

    /** Replaces the item at index. O(1). */
    public void set(int index, T value) {
        check(index);
        data[index] = value;
    }

    public int size() { return size; }
    public int capacity() { return data.length; }
    public boolean isEmpty() { return size == 0; }

    /** Empties the array. O(n). */
    public void clear() {
        for (int i = 0; i < size; i++) data[i] = null;
        size = 0;
    }

    /** Doubles the capacity, copying every item. O(n), rare. */
    private void grow() {
        Object[] bigger = new Object[data.length * 2];
        for (int i = 0; i < size; i++) bigger[i] = data[i];
        copies += size;
        data = bigger;
    }

    private void check(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index " + index + " (size " + size + ")");
    }
}
