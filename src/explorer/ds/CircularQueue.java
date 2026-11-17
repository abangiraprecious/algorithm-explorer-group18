package explorer.ds;

/**
 * FIFO queue stored in a circular array (ring buffer). Replaces java.util.Queue/ArrayDeque.
 * front points at the oldest item; rear = (front + size) % capacity is the next free slot.
 *
 * enqueue  amortised O(1) (resize unrolls the ring into a larger array)
 * dequeue  O(1)
 * peek     O(1)
 */
public class CircularQueue<T> {
    private Object[] data;
    private int front;
    private int size;
    public long ops;
    public boolean lastEnqueueResized;

    public CircularQueue() { this(4); }

    public CircularQueue(int capacity) {
        data = new Object[capacity < 1 ? 1 : capacity];
    }

    public void enqueue(T value) {
        lastEnqueueResized = false;
        if (size == data.length) {
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

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (size == 0) throw new IllegalStateException("queue underflow: the queue is empty");
        T v = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        size--;
        ops++;
        return v;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) throw new IllegalStateException("the queue is empty");
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
}
