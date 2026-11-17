package explorer.ds;

/**
 * FIFO queue on a fixed-size array WITHOUT wrap-around (the linear queue from the notes).
 * front = index of the oldest item, rear = index of the next free slot.
 *
 * Dequeue only moves front forward, so the slots before front are never used again.
 * Once rear reaches the end of the array the queue reports "full" even when most slots
 * are empty. This is the "false full" problem; CircularQueue fixes it by letting rear
 * wrap around to slot 0.
 *
 * enqueue / dequeue / front / size / isEmpty / isFull   O(1)
 */
public class LinearQueue<T> {
    private final Object[] data;
    private int front; // index of the oldest item
    private int rear;  // index of the next free slot
    public long ops;

    public LinearQueue(int capacity) {
        data = new Object[capacity < 1 ? 1 : capacity];
    }

    /** Adds value at index rear, then moves rear one place right. O(1). */
    public void enqueue(T value) {
        if (rear == data.length) {
            if (size() == data.length)
                throw new IllegalStateException("Queue is full: all " + data.length + " slots are in use, so " + value + " cannot be enqueued");
            throw new IllegalStateException("Queue is full (false full): rear has reached the end of the array, so " + value
                    + " cannot be enqueued, although only " + size() + " of " + data.length + " slots hold items. "
                    + "Slots 0.." + (front - 1) + " were freed by dequeue but a linear queue never goes back to them. A circular queue would wrap rear to slot 0.");
        }
        data[rear] = value;
        rear++;
        ops++;
    }

    /** Removes and returns the item at front, then moves front one place right. O(1), no shifting. */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (front == rear) throw new IllegalStateException("Queue underflow: the queue is empty, so there is nothing to dequeue");
        T v = (T) data[front];
        data[front] = null;
        front++;
        ops++;
        return v;
    }

    /** Returns the front item without removing it. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (front == rear) throw new IllegalStateException("The queue is empty, so there is no front element");
        ops++;
        return (T) data[front];
    }

    /** i-th item counted from the front. For drawing and display only. */
    @SuppressWarnings("unchecked")
    public T fromFront(int i) {
        if (i < 0 || i >= size()) throw new IndexOutOfBoundsException("index " + i);
        return (T) data[front + i];
    }

    /** Raw slot of the array (may be null). For drawing only. */
    @SuppressWarnings("unchecked")
    public T slot(int i) { return (T) data[i]; }

    public int front() { return front; }
    public int rear() { return rear; }
    public int size() { return rear - front; }
    public int capacity() { return data.length; }
    public boolean isEmpty() { return front == rear; }
    /** Full as soon as rear reaches the end of the array, whatever size() is. O(1). */
    public boolean isFull() { return rear == data.length; }
    /** True when the queue says it is full but some slots are actually free. */
    public boolean isFalseFull() { return isFull() && size() < data.length; }
}
