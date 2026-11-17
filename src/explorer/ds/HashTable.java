package explorer.ds;

/**
 * Hash table with String keys and separate chaining (replaces java.util.HashMap).
 * Uses its own polynomial hash: h = h*31 + c over the characters of the key.
 * Doubles the bucket array when load factor (size / buckets) exceeds 0.75.
 *
 * put / get / remove   O(1) on average, O(n) in the worst case (everything in one chain)
 */
public class HashTable<V> {

    public static final class Entry<V> {
        public final String key;
        public V value;
        public Entry<V> next;
        Entry(String key, V value, Entry<V> next) { this.key = key; this.value = value; this.next = next; }
    }

    private Entry<V>[] buckets;
    private int size;
    public long ops;
    public boolean lastPutResized;
    public int lastBucket = -1;

    public HashTable() { this(8); }

    @SuppressWarnings("unchecked")
    public HashTable(int capacity) {
        buckets = (Entry<V>[]) new Entry[capacity < 1 ? 1 : capacity];
    }

    /** Our own string hash, kept non-negative. */
    public static int hash(String key) {
        int h = 0;
        for (int i = 0; i < key.length(); i++) h = 31 * h + key.charAt(i);
        return h & 0x7fffffff;
    }

    public int indexFor(String key) { return hash(key) % buckets.length; }

    /** @return the previous value, or null if the key was new. */
    public V put(String key, V value) {
        lastPutResized = false;
        int b = indexFor(key);
        ops++; // hashing
        for (Entry<V> e = buckets[b]; e != null; e = e.next) {
            ops++;
            if (e.key.equals(key)) { V old = e.value; e.value = value; lastBucket = b; return old; }
        }
        buckets[b] = new Entry<V>(key, value, buckets[b]);
        size++;
        ops++;
        if (size > buckets.length * 3 / 4) { resize(); lastPutResized = true; }
        lastBucket = indexFor(key);
        return null;
    }

    /** Value stored for key, or null: hash to a bucket, then walk its chain. O(1) average. */
    public V get(String key) {
        int b = indexFor(key);
        lastBucket = b;
        ops++;
        for (Entry<V> e = buckets[b]; e != null; e = e.next) {
            ops++;
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }

    /** True if key is stored. O(1) average. */
    public boolean containsKey(String key) {
        int b = indexFor(key);
        for (Entry<V> e = buckets[b]; e != null; e = e.next) if (e.key.equals(key)) return true;
        return false;
    }

    /** Unlinks key from its chain and returns its value, or null. O(1) average. */
    public V remove(String key) {
        int b = indexFor(key);
        lastBucket = b;
        ops++;
        Entry<V> prev = null;
        for (Entry<V> e = buckets[b]; e != null; prev = e, e = e.next) {
            ops++;
            if (e.key.equals(key)) {
                if (prev == null) buckets[b] = e.next; else prev.next = e.next;
                size--;
                return e.value;
            }
        }
        return null;
    }

    /** Doubles the number of buckets and re-inserts every entry. O(n), rare. */
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<V>[] old = buckets;
        buckets = (Entry<V>[]) new Entry[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            Entry<V> e = old[i];
            while (e != null) {
                Entry<V> next = e.next;
                int b = indexFor(e.key);
                e.next = buckets[b];
                buckets[b] = e;
                ops++;
                e = next;
            }
        }
    }

    public Entry<V> bucket(int i) { return buckets[i]; }
    public int bucketCount() { return buckets.length; }
    public int size() { return size; }
}
