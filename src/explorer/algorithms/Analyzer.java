package explorer.algorithms;

import explorer.core.Input;
import explorer.core.Json;
import explorer.core.Rng;
import explorer.core.Tracer;
import explorer.ds.DynamicArray;

/**
 * Time-complexity analysis: runs algorithms on growing input sizes, records operation
 * counts and running times, then estimates the growth rate in two ways:
 *   1. the slope of log(ops) against log(n)  (≈1 for O(n), ≈2 for O(n²), ...)
 *   2. which model f(n) makes ops / f(n) the most constant (lowest coefficient of variation)
 */
public final class Analyzer {
    private Analyzer() {}

    public static final String[] ARRAY_KEYS = {"bubble", "selection", "insertion", "merge", "quick", "quick-random", "heap", "counting", "linear", "binary"};
    public static final String[] RECURSION_KEYS = {"fib-naive", "fib-memo", "hanoi", "factorial", "power"};

    // ------------------------------------------------------------------ input generation
    public static int[] generate(int n, String kind, long seed) {
        Rng r = new Rng(seed + n * 7919L);
        int[] a = new int[n];
        switch (kind) {
            case "sorted":
                for (int i = 0; i < n; i++) a[i] = i * 3;
                break;
            case "reversed":
                for (int i = 0; i < n; i++) a[i] = (n - i) * 3;
                break;
            case "nearly": {
                for (int i = 0; i < n; i++) a[i] = i * 3;
                int swaps = n / 20 + 1;           // ~5% of positions disturbed
                for (int s = 0; s < swaps && n > 1; s++) {
                    int i = r.nextInt(n), j = r.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            }
            case "few":
                for (int i = 0; i < n; i++) a[i] = r.nextInt(10);
                break;
            default: // random
                for (int i = 0; i < n; i++) a[i] = r.nextInt(n * 4 + 10);
        }
        return a;
    }

    private static boolean isRecursionKey(String k) {
        for (int i = 0; i < RECURSION_KEYS.length; i++) if (RECURSION_KEYS[i].equals(k)) return true;
        return false;
    }

    /** Largest n we allow for an algorithm on this input (keeps the page responsive). */
    private static int maxN(String key, String input) {
        switch (key) {
            case "bubble": case "selection": case "insertion": return 30000;
            case "quick": return (input.equals("sorted") || input.equals("reversed") || input.equals("few")) ? 100_000 : 1_000_000;
            case "fib-naive": return 32;
            case "hanoi": return 24;
            case "fib-memo": return 90;
            case "factorial": return 20;
            case "power": return 1_000_000_000;
            default: return 1_000_000;
        }
    }

    /** Which growth model theory predicts for this algorithm on this kind of input. */
    public static String expected(String key, String input) {
        switch (key) {
            case "bubble": return input.equals("sorted") ? "n" : "n²";
            case "insertion": return input.equals("sorted") ? "n" : input.equals("nearly") ? "n to n²" : "n²";
            case "selection": return "n²";
            case "quick": return (input.equals("sorted") || input.equals("reversed")) ? "n²"
                    : input.equals("few") ? "n² (many equal keys)" : "n log n";
            case "merge": case "heap": case "quick-random": return "n log n";
            case "counting": return "n + k";
            case "linear": return "n";
            case "binary": case "power": return "log n";
            case "fib-naive": return "φⁿ ≈ 1.618ⁿ";
            case "hanoi": return "2ⁿ";
            case "fib-memo": case "factorial": return "n";
            default: return "?";
        }
    }

    // ------------------------------------------------------------------ one measurement
    private static final class Point {
        int n; long ops, cmp, swp, wr, calls; long ns;
    }

    private static Point measure(String key, int n, String input, int trials) {
        Point p = new Point();
        p.n = n;
        long[] times = new long[trials];
        if (isRecursionKey(key)) {
            for (int t = 0; t < trials; t++) {
                Tracer tr = Tracer.silent();
                long start = System.nanoTime();
                runRecursion(key, n, tr);
                times[t] = System.nanoTime() - start;
                if (t == 0) fill(p, tr);
            }
        } else if (key.equals("linear") || key.equals("binary")) {
            int[] a = generate(n, input, 42);
            Sorting.merge(a, Tracer.silent());        // searching works on sorted data (not timed)
            int target = a.length == 0 ? 0 : a[a.length - 1] + 1;   // absent value = worst case
            int reps = 2000;
            for (int t = 0; t < trials; t++) {
                Tracer tr = Tracer.silent();
                long start = System.nanoTime();
                for (int r = 0; r < reps; r++) {
                    Tracer each = r == 0 ? tr : Tracer.silent();
                    Searching.run(key, a, target, each);
                }
                times[t] = (System.nanoTime() - start) / reps;
                if (t == 0) fill(p, tr);
            }
        } else {
            int[] base = generate(n, input, 42);
            for (int t = 0; t < trials; t++) {
                int[] a = Input.copy(base);
                Tracer tr = Tracer.silent();
                long start = System.nanoTime();
                Sorting.run(key, a, tr);
                times[t] = System.nanoTime() - start;
                if (t == 0) fill(p, tr);
                if (!Sorting.isSorted(a)) throw new IllegalStateException(key + " produced an unsorted result");
            }
        }
        p.ns = average(times);
        return p;
    }

    private static void fill(Point p, Tracer tr) {
        p.ops = tr.totalOps();
        p.cmp = tr.comparisons;
        p.swp = tr.swaps;
        p.wr = tr.writes;
        p.calls = tr.calls;
    }

    private static void runRecursion(String key, int n, Tracer t) {
        switch (key) {
            case "fib-naive": Recursion.fibNaive(n, t); break;
            case "fib-memo": Recursion.fibMemo(n, t); break;
            case "hanoi": Recursion.hanoi(n, t); break;
            case "factorial": Recursion.factorial(n, t); break;
            case "power": Recursion.power(1, n, t); break;  // base 1 never overflows; we only care about the call count
            default: throw new IllegalArgumentException(key);
        }
    }

    /** Average of the timed runs: the brief asks for the average of at least 3 runs. O(k). */
    private static long average(long[] v) {
        long sum = 0;
        for (int i = 0; i < v.length; i++) sum += v[i];
        return sum / v.length;
    }

    // ------------------------------------------------------------------ curve fitting
    private static double model(String m, double n) {
        double lg = Math.log(n) / Math.log(2);
        switch (m) {
            case "log n": return lg < 1 ? 1 : lg;
            case "n": return n;
            case "n log n": return n * (lg < 1 ? 1 : lg);
            case "n²": return n * n;
            case "n³": return n * n * n;
            case "2ⁿ": return Math.pow(2, n);
            default: return 1;
        }
    }

    private static final String[] MODELS = {"log n", "n", "n log n", "n²", "n³", "2ⁿ"};

    /** Picks the model whose ratio ops/f(n) varies least across the measured sizes. */
    private static String bestFit(DynamicArray<Point> pts, double[] cvOut) {
        String best = "?";
        double bestCv = Double.MAX_VALUE;
        for (int m = 0; m < MODELS.length; m++) {
            int count = 0;
            double sum = 0, sumSq = 0;
            for (int i = 0; i < pts.size(); i++) {
                Point p = pts.get(i);
                if (p.ops <= 0) continue;
                double f = model(MODELS[m], p.n);
                if (f <= 0 || Double.isInfinite(f)) continue;
                double ratio = p.ops / f;
                sum += ratio;
                sumSq += ratio * ratio;
                count++;
            }
            if (count < 2) continue;
            double mean = sum / count;
            double var = sumSq / count - mean * mean;
            double cv = Math.sqrt(var < 0 ? 0 : var) / mean;
            if (cv < bestCv) { bestCv = cv; best = MODELS[m]; }
        }
        cvOut[0] = bestCv;
        return best;
    }

    /** Least-squares slope of ln(y) against ln(n). */
    private static double logLogSlope(DynamicArray<Point> pts, boolean useTime) {
        int k = 0;
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (int i = 0; i < pts.size(); i++) {
            Point p = pts.get(i);
            double y = useTime ? p.ns : p.ops;
            if (y <= 0 || p.n <= 1) continue;
            double x = Math.log(p.n), ly = Math.log(y);
            sx += x; sy += ly; sxx += x * x; sxy += x * ly; k++;
        }
        if (k < 2) return Double.NaN;
        double d = k * sxx - sx * sx;
        if (d == 0) return Double.NaN;
        return (k * sxy - sx * sy) / d;
    }

    // ------------------------------------------------------------------ public entry point
    public static String run(DynamicArray<String> algos, int[] sizes, String input, int trials) {
        // Warm up the JIT so the first measured size is not unfairly slow
        for (int w = 0; w < 3; w++) {
            for (int i = 0; i < algos.size(); i++) {
                String k = algos.get(i);
                if (isRecursionKey(k)) runRecursion(k, k.equals("power") ? 1000 : 10, Tracer.silent());
                else if (k.equals("linear") || k.equals("binary")) {
                    int[] a = generate(2000, "sorted", 1);
                    Searching.run(k, a, -1, Tracer.silent());
                } else Sorting.run(k, generate(2000, "random", 1), Tracer.silent());
            }
        }

        StringBuilder series = new StringBuilder("[");
        for (int i = 0; i < algos.size(); i++) {
            String key = algos.get(i);
            DynamicArray<Point> pts = new DynamicArray<Point>();
            StringBuilder skipped = new StringBuilder("[");
            int limit = maxN(key, input);
            for (int s = 0; s < sizes.length; s++) {
                int n = sizes[s];
                if (n > limit) {
                    if (skipped.length() > 1) skipped.append(',');
                    skipped.append(n);
                    continue;
                }
                pts.add(measure(key, n, input, trials));
            }
            skipped.append(']');

            StringBuilder pj = new StringBuilder("[");
            for (int j = 0; j < pts.size(); j++) {
                Point p = pts.get(j);
                if (j > 0) pj.append(',');
                pj.append(Json.obj().put("n", p.n).put("ops", p.ops).put("cmp", p.cmp).put("swp", p.swp)
                        .put("wr", p.wr).put("calls", p.calls).put("ns", p.ns).end());
            }
            pj.append(']');

            double[] cv = new double[1];
            String fit = pts.size() >= 2 ? bestFit(pts, cv) : "?";
            if (i > 0) series.append(',');
            series.append(Json.obj().put("key", key).put("name", Catalog.name(key))
                    .raw("points", pj.toString()).raw("skipped", skipped.toString())
                    .put("limit", limit)
                    .put("slopeOps", logLogSlope(pts, false)).put("slopeTime", logLogSlope(pts, true))
                    .put("fit", fit).put("fitCv", cv[0]).put("expected", expected(key, input))
                    .raw("theory", Catalog.get(key)).end());
        }
        series.append(']');
        return Json.obj().put("input", input).put("trials", trials).raw("series", series.toString()).end();
    }
}
