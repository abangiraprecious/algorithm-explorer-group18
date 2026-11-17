package explorer.ds;

/** Growable list of primitive ints (no boxing). Used for parsed input and results. */
public class IntList {
    private int[] data = new int[16];
    private int size;

    public void add(int v) {
        if (size == data.length) {
            int[] bigger = new int[data.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = data[i];
            data = bigger;
        }
        data[size++] = v;
    }

    public int get(int i) {
        if (i < 0 || i >= size) throw new IndexOutOfBoundsException("index " + i);
        return data[i];
    }

    public int size() { return size; }

    public int[] toArray() {
        int[] out = new int[size];
        for (int i = 0; i < size; i++) out[i] = data[i];
        return out;
    }
}
