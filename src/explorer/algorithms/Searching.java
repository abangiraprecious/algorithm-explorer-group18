package explorer.algorithms;

import explorer.core.Json;
import explorer.core.Tracer;

/**
 * Linear search O(n), iterative binary search O(log n), and recursive binary search O(log n)
 * (whose recursion depth is also O(log n)).
 * Steps carry only the highlighted index and active range; the array is sent once.
 */
public final class Searching {
    private Searching() {}

    public static final String[] KEYS = {"linear", "binary", "binary-rec"};

    public static int run(String key, int[] a, int target, Tracer t) {
        switch (key) {
            case "linear": return linear(a, target, t);
            case "binary": return binary(a, target, t);
            case "binary-rec": return binaryRecursive(a, target, t);
            default: throw new IllegalArgumentException("Unknown search algorithm: " + key);
        }
    }

    private static void step(Tracer t, String type, String msg, int lo, int hi, int idx) {
        Json j = Json.obj().put("t", type).put("m", msg);
        if (lo >= 0 && lo <= hi) j.put("r", new int[]{lo, hi});
        if (idx >= 0) j.put("h", new int[]{idx});
        t.add(j.end());
    }

    public static int linear(int[] a, int target, Tracer t) {
        for (int i = 0; i < a.length; i++) {
            t.comparisons++;
            if (t.on()) step(t, "compare", "Check a[" + i + "]=" + a[i] + (a[i] == target ? " = " : " ≠ ") + target, -1, -1, i);
            if (a[i] == target) {
                if (t.on()) step(t, "found", "Found " + target + " at index " + i, -1, -1, i);
                return i;
            }
        }
        if (t.on()) step(t, "done", "Checked all " + a.length + " items: " + target + " is not in the array", -1, -1, -1);
        return -1;
    }

    public static int binary(int[] a, int target, Tracer t) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            t.comparisons++;
            t.arith++;
            if (t.on()) step(t, "compare", "Range a[" + lo + ".." + hi + "], middle a[" + mid + "]=" + a[mid]
                    + " vs target " + target, lo, hi, mid);
            if (a[mid] == target) {
                if (t.on()) step(t, "found", "Found " + target + " at index " + mid, lo, hi, mid);
                return mid;
            }
            if (a[mid] < target) {
                if (t.on()) step(t, "info", a[mid] + " < " + target + ", so discard the left half", mid + 1, hi, -1);
                lo = mid + 1;
            } else {
                if (t.on()) step(t, "info", a[mid] + " > " + target + ", so discard the right half", lo, mid - 1, -1);
                hi = mid - 1;
            }
        }
        if (t.on()) step(t, "done", "Range is empty: " + target + " is not in the array", -1, -1, -1);
        return -1;
    }

    public static int binaryRecursive(int[] a, int target, Tracer t) {
        int r = bs(a, target, 0, a.length - 1, t);
        if (r < 0 && t.on()) step(t, "done", target + " is not in the array", -1, -1, -1);
        return r;
    }

    private static int bs(int[] a, int target, int lo, int hi, Tracer t) {
        String label = !t.rec() ? null : "search(" + lo + ", " + hi + ")";
        int id = t.enter(label);
        int result;
        if (lo > hi) {
            if (t.on()) step(t, "info", "Base case: lo > hi, the range is empty", -1, -1, -1);
            result = -1;
        } else {
            int mid = lo + (hi - lo) / 2;
            t.comparisons++;
            t.arith++;
            if (t.on()) step(t, "compare", "Middle a[" + mid + "]=" + a[mid] + " vs target " + target, lo, hi, mid);
            if (a[mid] == target) {
                if (t.on()) step(t, "found", "Base case: found " + target + " at index " + mid, lo, hi, mid);
                result = mid;
            } else if (a[mid] < target) {
                result = bs(a, target, mid + 1, hi, t);
            } else {
                result = bs(a, target, lo, mid - 1, t);
            }
        }
        t.exit(id, label, t.rec() ? String.valueOf(result) : null);
        return result;
    }
}
