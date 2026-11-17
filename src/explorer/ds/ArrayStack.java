package explorer.ds;

/**
 * LIFO stack backed by a plain array (replaces java.util.Stack).
 * Two modes:
 *   fixed capacity (Module B) – the user sets the capacity; pushing onto a full stack is an overflow
 *   growable (used inside the program, e.g. the call-stack mirror) – the array doubles when full
 *
 * push   O(1) fixed; amortised O(1) growable (O(n) on the push that triggers a resize)
 * pop    O(1)
 * peek   O(1)
 * size / isEmpty / isFull   O(1)
 */
public class ArrayStack<T> {
    private Object[] data;
    private int top; // number of items; next free slot
    /** Basic operations performed (writes, reads, copies) – read by the explorer. */
    public long ops;
    /** Set to true by the push that caused the last resize. */
    public boolean lastPushResized;
    /** True for the Module B stack: the capacity never changes. */
    private final boolean fixed;

    public ArrayStack() { this(4); }

    public ArrayStack(int capacity) { this(capacity, false); }

    /** fixed = true gives a stack whose capacity is set once by the user. */
    public ArrayStack(int capacity, boolean fixed) {
        data = new Object[capacity < 1 ? 1 : capacity];
        this.fixed = fixed;
    }

    /** Puts value on top. O(1) (amortised O(1) when growable). */
    public void push(T value) {
        lastPushResized = false;
        if (top == data.length) {
            if (fixed) throw new IllegalStateException("Stack overflow: the stack is full (capacity " + data.length + "), so " + value + " cannot be pushed");
            Object[] bigger = new Object[data.length * 2];
            for (int i = 0; i < top; i++) { bigger[i] = data[i]; ops++; }
            data = bigger;
            lastPushResized = true;
        }
        data[top++] = value;
        ops++;
    }

    /** Removes and returns the top item. O(1). */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (top == 0) throw new IllegalStateException("Stack underflow: the stack is empty, so there is nothing to pop");
        T v = (T) data[--top];
        data[top] = null;
        ops++;
        return v;
    }

    /** Returns the top item without removing it. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (top == 0) throw new IllegalStateException("The stack is empty, so there is no top element");
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
    /** True when no more items fit without growing. O(1). */
    public boolean isFull() { return top == data.length; }
    public int size() { return top; }
    public int capacity() { return data.length; }
}
