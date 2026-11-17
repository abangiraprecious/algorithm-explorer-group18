package explorer.algorithms;

import explorer.core.Json;
import explorer.core.Rng;
import explorer.core.Tracer;

/**
 * Sorting algorithms, all written by hand and all sorting int[] in place
 * (merge sort and counting sort use extra arrays). The five from the brief are bubble,
 * selection, insertion, merge and quick sort (first-element pivot, plus a random-pivot version);
 * heap sort and counting sort are extras. Each one reports to a Tracer:
 *   comparisons – element-to-element comparisons
 *   swaps       – exchanges of two elements
 *   writes      – single element writes/shifts
 * and records steps (snapshots of the array with highlighted indices).
 */
public final class Sorting {
    private Sorting() {}

    public static final String[] KEYS = {"bubble", "selection", "insertion", "merge", "quick", "quick-random", "heap", "counting"};

    /** The algorithms run by the "Compare all" button: the five from the brief plus the random-pivot quick sort. */
    public static final String[] COMPARE_KEYS = {"bubble", "selection", "insertion", "merge", "quick", "quick-random"};

    /** Runs the sorting algorithm called key on a. Cost of the chosen algorithm. */
    public static void run(String key, int[] a, Tracer t) {
        switch (key) {
            case "bubble": bubble(a, t); break;
            case "selection": selection(a, t); break;
            case "insertion": insertion(a, t); break;
            case "merge": merge(a, t); break;
            case "quick": quick(a, t); break;
            case "quick-random": quickRandom(a, t); break;
            case "heap": heap(a, t); break;
            case "counting": counting(a, t); break;
            default: throw new IllegalArgumentException("Unknown sorting algorithm: " + key);
        }
    }

    // ------------------------------------------------------------------
    // Step snapshot helper
    // ------------------------------------------------------------------

    /**
     * Records one step. type: compare | swap | write | pivot | split | info | done
     * lo/hi = active sub-array (-1 for none), h = highlighted indices, pivot = pivot index or -1.
     */
    private static void snap(Tracer t, String type, String msg, int[] a, boolean[] fin,
                             int lo, int hi, int[] h, int pivot) {
        Json j = Json.obj().put("t", type).put("m", msg).put("a", a);
        if (h != null) j.put("h", h);
        if (lo >= 0) j.put("r", new int[]{lo, hi});
        if (pivot >= 0) j.put("pv", pivot);
        if (fin != null) {
            int count = 0;
            for (int i = 0; i < fin.length; i++) if (fin[i]) count++;
            int[] f = new int[count];
            int k = 0;
            for (int i = 0; i < fin.length; i++) if (fin[i]) f[k++] = i;
            j.put("f", f);
        }
        t.add(j.end());
    }

    /**
     * Same as snap, plus the two regions of the quick sort partition:
     * yellow = a[y0..y1] (smaller than the pivot), green = a[g0..g1] (greater than or equal to it).
     */
    private static void snapPart(Tracer t, String type, String msg, int[] a, boolean[] fin, int lo, int hi,
                                 int[] h, int pivot, int y0, int y1, int g0, int g1) {
        Json j = Json.obj().put("t", type).put("m", msg).put("a", a);
        if (h != null) j.put("h", h);
        j.put("r", new int[]{lo, hi});
        if (pivot >= 0) j.put("pv", pivot);
        if (y0 <= y1) j.put("y", new int[]{y0, y1});
        if (g0 <= g1) j.put("g", new int[]{g0, g1});
        if (fin != null) {
            int count = 0;
            for (int i = 0; i < fin.length; i++) if (fin[i]) count++;
            int[] f = new int[count];
            int k = 0;
            for (int i = 0; i < fin.length; i++) if (fin[i]) f[k++] = i;
            j.put("f", f);
        }
        t.add(j.end());
    }

    /** The array as ": [3, 9, 27]" for the pass messages; empty for arrays longer than 30. O(n). */
    private static String show(int[] a) {
        if (a.length > 30) return "";
        StringBuilder sb = new StringBuilder(": [");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(a[i]);
        }
        return sb.append(']').toString();
    }

    /** Exchanges a[i] and a[j] and counts one swap. O(1). */
    private static void swap(int[] a, int i, int j, Tracer t) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
        t.swaps++;
    }

    /** Marks every index as being in its final place (for drawing). O(n). */
    private static void markAll(boolean[] fin) {
        for (int i = 0; i < fin.length; i++) fin[i] = true;
    }

    // ------------------------------------------------------------------
    // Bubble sort – O(n^2), best case O(n) thanks to the early exit
    // ------------------------------------------------------------------
    public static void bubble(int[] a, Tracer t) {
        int n = a.length;
        boolean[] fin = new boolean[n];
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                t.comparisons++;
                if (t.on()) snap(t, "compare", "Pass " + (i + 1) + ": compare a[" + j + "]=" + a[j]
                        + " with a[" + (j + 1) + "]=" + a[j + 1], a, fin, -1, -1, new int[]{j, j + 1}, -1);
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1, t);
                    swapped = true;
                    if (t.on()) snap(t, "swap", a[j + 1] + " > " + a[j] + ", so swap them", a, fin,
                            -1, -1, new int[]{j, j + 1}, -1);
                }
            }
            fin[n - 1 - i] = true;
            if (!swapped) {
                markAll(fin);
                if (t.onKey()) snap(t, "pass", "After pass " + (i + 1) + show(a) + ". No swaps in this pass, so the array is already sorted. Stop early.",
                        a, fin, -1, -1, null, -1);
                break;
            }
            if (t.onKey()) snap(t, "pass", "After pass " + (i + 1) + show(a) + ". " + a[n - 1 - i]
                    + " has bubbled to its final place", a, fin, -1, -1, new int[]{n - 1 - i}, -1);
        }
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    // ------------------------------------------------------------------
    // Selection sort – always O(n^2) comparisons, at most n-1 swaps
    // ------------------------------------------------------------------
    public static void selection(int[] a, Tracer t) {
        int n = a.length;
        boolean[] fin = new boolean[n];
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            if (t.on()) snap(t, "info", "Find the smallest value in a[" + i + ".." + (n - 1) + "]",
                    a, fin, i, n - 1, new int[]{i}, -1);
            for (int j = i + 1; j < n; j++) {
                t.comparisons++;
                if (t.on()) snap(t, "compare", "Compare a[" + j + "]=" + a[j] + " with current minimum a["
                        + min + "]=" + a[min], a, fin, i, n - 1, new int[]{j, min}, -1);
                if (a[j] < a[min]) {
                    min = j;
                    if (t.on()) snap(t, "info", "New minimum: " + a[min] + " at index " + min,
                            a, fin, i, n - 1, new int[]{min}, -1);
                }
            }
            if (min != i) {
                swap(a, i, min, t);
                if (t.on()) snap(t, "swap", "Swap the minimum into position " + i, a, fin, i, n - 1,
                        new int[]{i, min}, -1);
            }
            fin[i] = true;
            if (t.onKey()) snap(t, "pass", "After pass " + (i + 1) + show(a) + ". " + a[i] + " is in its final place a[" + i + "]",
                    a, fin, -1, -1, new int[]{i}, -1);
        }
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    // ------------------------------------------------------------------
    // Insertion sort – O(n^2) worst/average, O(n) when already sorted
    // ------------------------------------------------------------------
    public static void insertion(int[] a, Tracer t) {
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int key = a[i];
            int j = i - 1;
            if (t.on()) snap(t, "info", "Take key " + key + " (a[" + i + "]) and insert it into the sorted part a[0.."
                    + (i - 1) + "]", a, null, 0, i, new int[]{i}, -1);
            while (j >= 0) {
                t.comparisons++;
                if (t.on()) snap(t, "compare", "Compare a[" + j + "]=" + a[j] + " with key " + key,
                        a, null, 0, i, new int[]{j}, -1);
                if (a[j] > key) {
                    a[j + 1] = a[j];
                    t.writes++;
                    if (t.on()) snap(t, "write", a[j] + " > " + key + ", so shift it right to a[" + (j + 1) + "]",
                            a, null, 0, i, new int[]{j, j + 1}, -1);
                    j--;
                } else break;
            }
            a[j + 1] = key;
            t.writes++;
            if (t.on()) snap(t, "write", "Place key " + key + " at a[" + (j + 1) + "]", a, null, 0, i,
                    new int[]{j + 1}, -1);
            if (t.onKey()) snap(t, "pass", "After pass " + i + show(a) + ". a[0.." + i + "] is now sorted", a, null, 0, i,
                    new int[]{j + 1}, -1);
        }
        boolean[] fin = new boolean[n];
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    // ------------------------------------------------------------------
    // Merge sort – recursive divide and conquer, O(n log n) always, O(n) extra space
    // ------------------------------------------------------------------
    public static void merge(int[] a, Tracer t) {
        int[] aux = new int[a.length];
        if (a.length > 0) mergeSort(a, aux, 0, a.length - 1, t);
        boolean[] fin = new boolean[a.length];
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    /** Sorts a[lo..hi]: split in half, sort both halves, merge them through aux. T(n) = 2T(n/2) + O(n) = O(n log n). */
    private static void mergeSort(int[] a, int[] aux, int lo, int hi, Tracer t) {
        t.enterQuiet();
        if (lo >= hi) {           // base case: one element is already sorted
            t.exitQuiet();
            return;
        }
        int mid = lo + (hi - lo) / 2;
        if (t.on()) snap(t, "split", "Depth " + t.depth + ": split a[" + lo + ".." + hi + "] into a[" + lo + ".."
                + mid + "] and a[" + (mid + 1) + ".." + hi + "]", a, null, lo, hi, null, -1);
        mergeSort(a, aux, lo, mid, t);
        mergeSort(a, aux, mid + 1, hi, t);

        for (int k = lo; k <= hi; k++) aux[k] = a[k];
        t.writes += hi - lo + 1;

        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            String why;
            if (i > mid) { a[k] = aux[j++]; why = "left half used up"; }
            else if (j > hi) { a[k] = aux[i++]; why = "right half used up"; }
            else {
                t.comparisons++;
                if (aux[j] < aux[i]) { why = aux[j] + " < " + aux[i]; a[k] = aux[j++]; }
                else { why = aux[i] + " ≤ " + aux[j]; a[k] = aux[i++]; } // ≤ keeps it stable
            }
            t.writes++;
            if (t.on()) snap(t, "write", "Merge a[" + lo + ".." + hi + "]: write " + a[k] + " to a[" + k + "] (" + why + ")",
                    a, null, lo, hi, new int[]{k}, -1);
        }
        if (t.onKey()) snap(t, "pass", "After merging a[" + lo + ".." + hi + "]" + show(a), a, null, lo, hi, null, -1);
        t.exitQuiet();
    }

    // ------------------------------------------------------------------
    // Quick sort – first element as pivot, with the "yellow/green" partition from class.
    // While a[lo..hi] is partitioned around pivot = a[lo]:
    //   yellow = a[lo+1..m]    values smaller than the pivot
    //   green  = a[m+1..k-1]   values greater than or equal to the pivot
    //   a[k..hi]               not looked at yet
    // A smaller a[k] is swapped with the first green item, so yellow grows by one.
    // At the end the pivot is swapped with the last yellow item, into its final place m.
    // O(n log n) on average; O(n²) on sorted or reversed input, where every split is 0 : n−1.
    // ------------------------------------------------------------------
    public static void quick(int[] a, Tracer t) {
        boolean[] fin = new boolean[a.length];
        quickSort(a, 0, a.length - 1, t, fin, null);
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    // ------------------------------------------------------------------
    // Quick sort with a random pivot – before each partition a random element of a[lo..hi]
    // is swapped to a[lo], then the same yellow/green partition runs. No input order is
    // always bad any more, so the expected time is O(n log n) for every input.
    // ------------------------------------------------------------------
    public static void quickRandom(int[] a, Tracer t) {
        boolean[] fin = new boolean[a.length];
        quickSort(a, 0, a.length - 1, t, fin, new Rng(System.nanoTime()));
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    /** Recursive part of both quick sorts: partition, then sort each side. rng == null means first-element pivot. */
    private static void quickSort(int[] a, int lo, int hi, Tracer t, boolean[] fin, Rng rng) {
        t.enterQuiet();
        if (lo < hi) {
            int p = partition(a, lo, hi, t, fin, rng);
            fin[p] = true;
            if (t.onKey()) snap(t, "pass", "After partitioning a[" + lo + ".." + hi + "]" + show(a) + ". Pivot " + a[p]
                    + " is now in its final position " + p + "; recurse on both sides.", a, fin, lo, hi, null, p);
            quickSort(a, lo, p - 1, t, fin, rng);
            quickSort(a, p + 1, hi, t, fin, rng);
        } else if (lo == hi) {
            fin[lo] = true;
        }
        t.exitQuiet();
    }

    /** Yellow/green partition of a[lo..hi] around the first element. Returns the pivot's final index. O(n). */
    private static int partition(int[] a, int lo, int hi, Tracer t, boolean[] fin, Rng rng) {
        if (rng != null) {
            int r = rng.between(lo, hi);
            if (r != lo) {
                swap(a, lo, r, t);
                if (t.on()) snap(t, "swap", "Random pivot: swap a[" + r + "]=" + a[lo] + " to the front, a[" + lo + "]",
                        a, fin, lo, hi, new int[]{lo, r}, -1);
            }
        }
        int pivot = a[lo];
        int m = lo;                         // last index of the yellow (smaller) part
        if (t.on()) snapPart(t, "pivot", "Depth " + t.depth + ": partition a[" + lo + ".." + hi + "] around the first element, pivot " + pivot,
                a, fin, lo, hi, null, lo, 1, 0, 1, 0);
        for (int k = lo + 1; k <= hi; k++) {
            t.comparisons++;
            if (t.on()) snapPart(t, "compare", "Compare a[" + k + "]=" + a[k] + " with pivot " + pivot,
                    a, fin, lo, hi, new int[]{k}, lo, lo + 1, m, m + 1, k - 1);
            if (a[k] < pivot) {
                m++;
                if (m != k) {
                    swap(a, m, k, t);       // the first green item moves to the end of green
                    if (t.on()) snapPart(t, "swap", a[m] + " < " + pivot + ": swap it with the first green item, so yellow grows (swap a[" + m + "] and a[" + k + "])",
                            a, fin, lo, hi, new int[]{m, k}, lo, lo + 1, m, m + 1, k);
                } else if (t.on()) snapPart(t, "info", a[m] + " < " + pivot + ": it joins the yellow part",
                        a, fin, lo, hi, new int[]{m}, lo, lo + 1, m, m + 1, k);
            } else if (t.on()) snapPart(t, "info", a[k] + " ≥ " + pivot + ": it joins the green part",
                    a, fin, lo, hi, new int[]{k}, lo, lo + 1, m, m + 1, k);
        }
        if (m != lo) {
            swap(a, lo, m, t);
            if (t.on()) snapPart(t, "swap", "Swap the pivot with the last yellow item (a[" + lo + "] and a[" + m + "]): yellow | pivot | green",
                    a, fin, lo, hi, new int[]{lo, m}, m, lo, m - 1, m + 1, hi);
        }
        return m;
    }

    // ------------------------------------------------------------------
    // Heap sort – build a max-heap, then repeatedly move the max to the end. O(n log n), in place.
    // ------------------------------------------------------------------
    public static void heap(int[] a, Tracer t) {
        int n = a.length;
        boolean[] fin = new boolean[n];
        if (t.on()) snap(t, "info", "Phase 1: build a max-heap (parent ≥ children) from the bottom up", a, fin, 0, n - 1, null, -1);
        for (int i = n / 2 - 1; i >= 0; i--) siftDown(a, i, n, t, fin);
        if (t.on()) snap(t, "info", "Max-heap built: the largest value " + (n > 0 ? a[0] : 0) + " is at the root a[0]",
                a, fin, 0, n - 1, new int[]{0}, -1);
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end, t);
            fin[end] = true;
            if (t.on()) snap(t, "swap", "Move the max " + a[end] + " to its final place a[" + end + "]", a, fin, 0, end,
                    new int[]{0, end}, -1);
            siftDown(a, 0, end, t, fin);
            if (t.onKey()) snap(t, "pass", "After pass " + (n - end) + show(a) + ". The heap is repaired; a[" + end + ".." + (n - 1) + "] is final",
                    a, fin, 0, end - 1, null, -1);
        }
        markAll(fin);
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    /** Moves a[i] down the max-heap a[0..n-1] until it is larger than both children. O(log n). */
    private static void siftDown(int[] a, int i, int n, Tracer t, boolean[] fin) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, largest = i;
            if (l < n) {
                t.comparisons++;
                if (a[l] > a[largest]) largest = l;
            }
            if (r < n) {
                t.comparisons++;
                if (a[r] > a[largest]) largest = r;
            }
            if (t.on() && l < n) snap(t, "compare", "Sift down: compare a[" + i + "]=" + a[i] + " with its children",
                    a, fin, 0, n - 1, r < n ? new int[]{i, l, r} : new int[]{i, l}, -1);
            if (largest == i) return;
            swap(a, i, largest, t);
            if (t.on()) snap(t, "swap", "Swap with the larger child a[" + largest + "]", a, fin, 0, n - 1,
                    new int[]{i, largest}, -1);
            i = largest;
        }
    }

    // ------------------------------------------------------------------
    // Counting sort – not comparison based. O(n + k) time and space, k = max - min + 1.
    // ------------------------------------------------------------------
    public static void counting(int[] a, Tracer t) {
        int n = a.length;
        if (n == 0) return;
        int min = a[0], max = a[0];
        for (int i = 1; i < n; i++) {
            t.comparisons += 2;
            if (a[i] < min) min = a[i];
            if (a[i] > max) max = a[i];
        }
        long range = (long) max - min + 1;
        if (range > 1_000_000)
            throw new IllegalArgumentException("Counting sort needs max − min ≤ 1,000,000; your range is " + range
                    + ". Use a comparison sort for widely spread values.");
        int k = (int) range;
        int[] count = new int[k];
        if (t.on()) snap(t, "info", "Range is " + min + ".." + max + " so k = " + k + " counters", a, null, -1, -1, null, -1);
        for (int i = 0; i < n; i++) {
            count[a[i] - min]++;
            t.writes++;
            if (t.on()) snap(t, "write", "Count " + a[i] + " (count[" + (a[i] - min) + "] = " + count[a[i] - min] + ")",
                    a, null, -1, -1, new int[]{i}, -1);
        }
        for (int v = 1; v < k; v++) { count[v] += count[v - 1]; t.writes++; }
        if (t.on()) snap(t, "info", "Prefix sums turn counts into final positions", a, null, -1, -1, null, -1);
        int[] out = new int[n];
        for (int i = n - 1; i >= 0; i--) {      // right to left keeps it stable
            out[--count[a[i] - min]] = a[i];
            t.writes++;
        }
        boolean[] fin = new boolean[n];
        for (int i = 0; i < n; i++) {
            a[i] = out[i];
            t.writes++;
            fin[i] = true;
            if (t.on()) snap(t, "write", "Copy " + a[i] + " back to a[" + i + "]", a, fin, -1, -1, new int[]{i}, -1);
        }
        if (t.on()) snap(t, "done", "Sorted", a, fin, -1, -1, null, -1);
    }

    /** True if a is in non-decreasing order (used to verify every result). */
    public static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }
}
