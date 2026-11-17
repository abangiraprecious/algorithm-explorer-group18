package explorer.ds;

/**
 * FIFO queue stored in a circular array (ring buffer). Replaces java.util.Queue/ArrayDeque.
 * front points at the oldest item; rear = (front + size) % capacity is the next free slot.
 * Because rear wraps around to slot 0, slots freed by dequeue are reused: this is what
 * fixes the "false full" problem of the linear queue.
 * Two modes: fixed capacity (Module B, enqueue on a full queue is an error) or growable.
 *
 * enqueue  O(1) fixed; amortised O(1) growable (resize unrolls the ring into a larger array)
 * dequeue  O(1)
 * peek     O(1)
 * size / isEmpty / isFull   O(1)
 */
public class CircularQueue<T> {
    private Object[] data;
    private int front;
    private int size;
    public long ops;
    public boolean lastEnqueueResized;
    /** True for the Module B queue: the capacity never changes. */
    private final boolean fixed;

    public CircularQueue() { this(4); }

    public CircularQueue(int capacity) { this(capacity, false); }

    /** fixed = true gives a queue whose capacity is set once by the user. */
    public CircularQueue(int capacity, boolean fixed) {
        data = new Object[capacity < 1 ? 1 : capacity];
        this.fixed = fixed;
    }

    /** Adds value at the rear; rear moves forward with modulo, so it wraps to slot 0. O(1). */
    public void enqueue(T value) {
        lastEnqueueResized = false;
        if (size == data.length) {
            if (fixed) throw new IllegalStateException("Queue is full: all " + data.length + " slots are in use, so " + value + " cannot be enqueued");
            Object[] bigger = new Object[data.length * 2];
            for (int i = 0; i < size; i++) { bigger[i] = data[(front + i) % data.length]; ops++; }
            data = bigger;
            front = 0;
            lastEnqueueResized = true;
        }
        int rear = (front + size) % data.length;
        data[rear] = value;
        size++;
        ops++;
    }

    /** Removes and returns the front item; front moves forward with modulo. O(1), no shifting. */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) throw new IllegalStateException("Queue underflow: the queue is empty, so there is nothing to dequeue");
        T v = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        size--;
        ops++;
        return v;
    }

    /** Returns the front item without removing it. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) throw new IllegalStateException("The queue is empty, so there is no front element");
        ops++;
        return (T) data[front];
    }

    /** i-th item counted from the front. */
    @SuppressWarnings("unchecked")
    public T fromFront(int i) {
        if (i < 0 || i >= size) throw new IndexOutOfBoundsException("index " + i);
        return (T) data[(front + i) % data.length];
    }

    /** Raw slot of the underlying ring buffer (may be null). For drawing only. */
    @SuppressWarnings("unchecked")
    public T slot(int i) { return (T) data[i]; }

    public int front() { return front; }
    public int rear() { return (front + size) % data.length; }
    public int size() { return size; }
    public int capacity() { return data.length; }
    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == data.length; }
}
