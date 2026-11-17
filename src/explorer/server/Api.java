package explorer.server;

import explorer.algorithms.Analyzer;
import explorer.algorithms.Catalog;
import explorer.algorithms.DataStructures;
import explorer.algorithms.Recursion;
import explorer.algorithms.Searching;
import explorer.algorithms.Sorting;
import explorer.core.Input;
import explorer.core.Json;
import explorer.core.Tracer;
import explorer.ds.DynamicArray;

/**
 * Back-end request handlers. Each takes the user's form input, runs the algorithm,
 * and returns a JSON string containing: result, steps, operation counts, running time
 * and the theoretical complexity.
 */
public final class Api {
    private Api() {}

    private static final int TIMING_TRIALS = 5;

    private static boolean oneOf(String v, String[] options) {
        for (int i = 0; i < options.length; i++) if (options[i].equals(v)) return true;
        return false;
    }

    private static long median(long[] v) {
        // insertion sort on our own copy – no library sort anywhere
        long[] a = new long[v.length];
        for (int i = 0; i < v.length; i++) a[i] = v[i];
        for (int i = 1; i < a.length; i++) {
            long k = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > k) { a[j + 1] = a[j]; j--; }
            a[j + 1] = k;
        }
        return a[a.length / 2];
    }

    // ------------------------------------------------------------------ /run/sort
    public static String sort(Params p) {
        String algo = p.require("algo", "an algorithm");
        if (!oneOf(algo, Sorting.KEYS)) throw new IllegalArgumentException("Unknown sorting algorithm: " + algo);
        int[] input = Input.parseInts(p.require("values", "the numbers to sort"), 100000);
        int n = input.length;

        // 1) traced run – steps are kept for small inputs only (each step stores the whole array)
        boolean traceOn = n <= 120;
        Tracer traced = new Tracer(traceOn, 3000);
        int[] work = Input.copy(input);
        Sorting.run(algo, work, traced);
        if (!Sorting.isSorted(work)) throw new IllegalStateException(algo + " did not sort the array");

        // 2) timed runs – no step recording, median of several trials
        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            int[] c = Input.copy(input);
            Tracer silent = Tracer.silent();
            long start = System.nanoTime();
            Sorting.run(algo, c, silent);
            times[t] = System.nanoTime() - start;
        }

        return Json.obj().put("category", "sort").put("algo", algo).put("n", n)
                .raw("theory", Catalog.get(algo))
                .put("input", n <= 2000 ? input : null)
                .put("output", n <= 2000 ? work : null)
                .put("result", "Sorted " + n + " number(s)" + (n > 2000 ? " (too many to list)" : ""))
                .raw("counts", traced.countsJson())
                .put("timeNs", median(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", traceOn).put("truncated", traced.truncated())
                .raw("steps", traced.stepsJson()).end();
    }

    // ------------------------------------------------------------------ /run/search
    public static String search(Params p) {
        String algo = p.require("algo", "an algorithm");
        if (!oneOf(algo, Searching.KEYS)) throw new IllegalArgumentException("Unknown search algorithm: " + algo);
        int[] a = Input.parseInts(p.require("values", "the numbers to search in"), 100000);
        int target = Input.parseInt(p.require("target", "the value to look for").trim(), "target");

        String note = null;
        if (!algo.equals("linear") && !Sorting.isSorted(a)) {
            Sorting.merge(a, Tracer.silent());
            note = "Binary search needs sorted data, so the input was sorted first with merge sort (not counted below).";
        }

        Tracer traced = new Tracer(true, 3000);
        int idx = Searching.run(algo, a, target, traced);

        int reps = 200;
        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            long start = System.nanoTime();
            for (int r = 0; r < reps; r++) Searching.run(algo, a, target, Tracer.silent());
            times[t] = (System.nanoTime() - start) / reps;
        }

        return Json.obj().put("category", "search").put("algo", algo).put("n", a.length)
                .raw("theory", Catalog.get(algo))
                .put("array", a.length <= 2000 ? a : null).put("target", target).put("index", idx)
                .put("note", note)
                .put("result", idx >= 0 ? "Found " + target + " at index " + idx : target + " was not found")
                .raw("counts", traced.countsJson())
                .put("timeNs", median(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", a.length <= 2000).put("truncated", traced.truncated())
                .raw("steps", a.length <= 2000 ? traced.stepsJson() : "[]").end();
    }

    // ------------------------------------------------------------------ /run/recursion
    private interface RecRun { String run(Tracer t); }

    public static String recursion(Params p) {
        final String algo = p.require("algo", "an algorithm");
        final String arg = p.require("arg", "an input value").trim();
        RecRun job;
        String extraKey = null;
        final DynamicArray<String>[] perms = newPermHolder();

        switch (algo) {
            case "factorial": {
                final int n = Input.intInRange(arg, "n", 0, 20);
                job = t -> n + "! = " + Recursion.factorial(n, t);
                break;
            }
            case "fib-naive": {
                final int n = Input.intInRange(arg, "n", 0, 32);
                job = t -> "fib(" + n + ") = " + Recursion.fibNaive(n, t);
                break;
            }
            case "fib-memo": {
                final int n = Input.intInRange(arg, "n", 0, 92);
                job = t -> "fib(" + n + ") = " + Recursion.fibMemo(n, t);
                break;
            }
            case "fib-iter": {
                final int n = Input.intInRange(arg, "n", 0, 92);
                job = t -> "fib(" + n + ") = " + Recursion.fibIterative(n, t);
                break;
            }
            case "hanoi": {
                final int n = Input.intInRange(arg, "number of disks", 1, 20);
                job = t -> Recursion.hanoi(n, t) + " moves for " + n + " disk(s)";
                break;
            }
            case "power": {
                DynamicArray<String> tok = Input.tokens(arg);
                if (tok.size() != 2) throw new IllegalArgumentException("Enter a base and an exponent, e.g. 3 13");
                final int x = Input.intInRange(tok.get(0), "base", -1000, 1000);
                final int n = Input.intInRange(tok.get(1), "exponent", 0, 100000);
                job = t -> x + "^" + n + " = " + Recursion.power(x, n, t)
                        + "  (" + (n == 0 ? 0 : n - 1) + " multiplications the slow way)";
                break;
            }
            case "gcd": {
                DynamicArray<String> tok = Input.tokens(arg);
                if (tok.size() != 2) throw new IllegalArgumentException("Enter two whole numbers, e.g. 1071 462");
                final int a = Input.intInRange(tok.get(0), "a", 0, 1_000_000_000);
                final int b = Input.intInRange(tok.get(1), "b", 0, 1_000_000_000);
                if (a == 0 && b == 0) throw new IllegalArgumentException("gcd(0, 0) is undefined. Enter at least one non-zero number.");
                job = t -> "gcd(" + a + ", " + b + ") = " + Recursion.gcd(a, b, t);
                break;
            }
            case "permutations": {
                if (arg.length() > 7) throw new IllegalArgumentException("Use at most 7 characters (7! = 5040 permutations).");
                job = t -> {
                    DynamicArray<String> out = Recursion.permutations(arg, t);
                    if (t.rec()) perms[0] = out;
                    return out.size() + " permutation(s) of \"" + arg + "\"";
                };
                extraKey = "list";
                break;
            }
            case "palindrome": {
                if (arg.length() > 2000) throw new IllegalArgumentException("Keep the text under 2000 characters.");
                StringBuilder clean = new StringBuilder();
                for (int i = 0; i < arg.length(); i++) {
                    char c = arg.charAt(i);
                    if (c >= 'A' && c <= 'Z') clean.append((char) (c + 32));
                    else if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) clean.append(c);
                }
                final String s = clean.toString();
                if (s.length() == 0) throw new IllegalArgumentException("Enter some letters or digits.");
                job = t -> "\"" + arg + "\" " + (Recursion.palindrome(s, t) ? "is" : "is not")
                        + " a palindrome (checked \"" + s + "\": letters and digits only, lower case)";
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown recursive algorithm: " + algo);
        }

        Tracer traced = new Tracer(true, 2500);
        String result = job.run(traced);

        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            Tracer silent = Tracer.silent();
            long start = System.nanoTime();
            job.run(silent);
            times[t] = System.nanoTime() - start;
        }

        Json j = Json.obj().put("category", "recursion").put("algo", algo).put("arg", arg)
                .raw("theory", Catalog.get(algo)).put("result", result)
                .raw("counts", traced.countsJson())
                .put("timeNs", median(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", true).put("truncated", traced.truncated())
                .raw("steps", traced.stepsJson());
        if (extraKey != null && perms[0] != null) j.raw(extraKey, Recursion.stringsJson(perms[0], 5040));
        return j.end();
    }

    @SuppressWarnings("unchecked")
    private static DynamicArray<String>[] newPermHolder() { return (DynamicArray<String>[]) new DynamicArray[1]; }

    // ------------------------------------------------------------------ /run/ds
    public static String ds(Params p) {
        String kind = p.require("kind", "a data structure");
        if (!oneOf(kind, DataStructures.KINDS)) throw new IllegalArgumentException("Unknown data structure: " + kind);
        String script = p.require("script", kind.equals("brackets") || kind.equals("expression") ? "some text" : "at least one command");

        DataStructures traced = new DataStructures(true);
        traced.run(kind, script);

        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            DataStructures silent = new DataStructures(false);
            long start = System.nanoTime();
            silent.run(kind, script);
            times[t] = System.nanoTime() - start;
        }

        String counts = Json.obj().put("total", traced.totalOps).put("operations", traced.stepCount()).end();
        return Json.obj().put("category", "ds").put("algo", kind)
                .raw("theory", Catalog.get("ds-" + kind)).put("result", traced.summary)
                .raw("counts", counts)
                .put("timeNs", median(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", true).put("truncated", traced.stepCount() >= 2000)
                .raw("steps", traced.stepsJson()).end();
    }

    // ------------------------------------------------------------------ /run/analyze
    public static String analyze(Params p) {
        DynamicArray<String> algos = Input.tokens(p.require("algos", "at least one algorithm"));
        for (int i = 0; i < algos.size(); i++) {
            if (!oneOf(algos.get(i), Analyzer.ARRAY_KEYS) && !oneOf(algos.get(i), Analyzer.RECURSION_KEYS))
                throw new IllegalArgumentException("Unknown algorithm for analysis: " + algos.get(i));
        }
        if (algos.size() > 9) throw new IllegalArgumentException("Pick at most 9 algorithms at a time.");
        int[] sizes = Input.parseInts(p.require("sizes", "the input sizes"), 12);
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] < 1 || sizes[i] > 1_000_000)
                throw new IllegalArgumentException("Each size must be between 1 and 1,000,000 (found " + sizes[i] + ").");
        }
        // sort the sizes with our own insertion sort so the chart reads left to right
        for (int i = 1; i < sizes.length; i++) {
            int k = sizes[i], j = i - 1;
            while (j >= 0 && sizes[j] > k) { sizes[j + 1] = sizes[j]; j--; }
            sizes[j + 1] = k;
        }
        String input = p.getOr("input", "random");
        if (!oneOf(input, new String[]{"random", "sorted", "reversed", "nearly", "few"}))
            throw new IllegalArgumentException("Unknown input type: " + input);
        int trials = Input.intInRange(p.getOr("trials", "3"), "trials", 1, 7);
        return Analyzer.run(algos, sizes, input, trials);
    }
}
