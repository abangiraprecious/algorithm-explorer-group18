package explorer.ds;

/**
 * LIFO stack backed by a plain array that doubles when full (replaces java.util.Stack).
 *
 * push   amortised O(1) (O(n) on the push that triggers a resize)
 * pop    O(1)
 * peek   O(1)
 */
public class ArrayStack<T> {
    private Object[] data;
    private int top; // number of items; next free slot
    /** Basic operations performed (writes, reads, copies) – read by the explorer. */
    public long ops;
    /** Set to true by the push that caused the last resize. */
    public boolean lastPushResized;

    public ArrayStack() { this(4); }

    public ArrayStack(int capacity) {
        data = new Object[capacity < 1 ? 1 : capacity];
    }

    public void push(T value) {
        lastPushResized = false;
        if (top == data.length) {
            Object[] bigger = new Object[data.length * 2];
            for (int i = 0; i < top; i++) { bigger[i] = data[i]; ops++; }
            data = bigger;
            lastPushResized = true;
        }
        data[top++] = value;
        ops++;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (top == 0) throw new IllegalStateException("stack underflow: the stack is empty");
        T v = (T) data[--top];
        data[top] = null;
        ops++;
        return v;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (top == 0) throw new IllegalStateException("the stack is empty");
        ops++;
        return (T) data[top - 1];
    }

    /** Item at position i counted from the bottom (0 = bottom). For drawing only. */
    @SuppressWarnings("unchecked")
    public T fromBottom(int i) {
        if (i < 0 || i >= top) throw new IndexOutOfBoundsException("index " + i);
        return (T) data[i];
    }

    public boolean isEmpty() { return top == 0; }
    public int size() { return top; }
    public int capacity() { return data.length; }
}
