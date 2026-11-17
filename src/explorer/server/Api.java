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

    /** Average of the timed runs (the brief asks for the average of repeated runs). O(k) for k runs. */
    private static long average(long[] v) {
        long sum = 0;
        for (int i = 0; i < v.length; i++) sum += v[i];
        return sum / v.length;
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

        // 2) timed runs – no step recording, average of several trials
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
                .put("timeNs", average(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", traceOn).put("truncated", traced.truncated())
                .raw("steps", traced.stepsJson()).end();
    }

    // ------------------------------------------------------------------ /run/compare  ("Compare all" button)
    /**
     * Runs bubble, selection, insertion, merge and quick sort (both pivots) on the SAME input and
     * returns one row per algorithm: comparisons, swaps, writes and the average time of 3 runs.
     */
    public static String compare(Params p) {
        int[] input = Input.parseInts(p.require("values", "the numbers to sort"), 20000);
        int trials = 3;
        StringBuilder rows = new StringBuilder("[");
        for (int k = 0; k < Sorting.COMPARE_KEYS.length; k++) {
            String algo = Sorting.COMPARE_KEYS[k];
            // warm up the JIT first so the first algorithm in the table is not unfairly slow
            int warm = input.length <= 2000 ? 20 : 1;
            for (int w = 0; w < warm; w++) Sorting.run(algo, Input.copy(input), Tracer.silent());
            Tracer counted = Tracer.silent();
            int[] work = Input.copy(input);
            Sorting.run(algo, work, counted);
            if (!Sorting.isSorted(work)) throw new IllegalStateException(algo + " did not sort the array");
            long[] times = new long[trials];
            for (int t = 0; t < trials; t++) {
                int[] c = Input.copy(input);
                long start = System.nanoTime();
                Sorting.run(algo, c, Tracer.silent());
                times[t] = System.nanoTime() - start;
            }
            if (k > 0) rows.append(',');
            rows.append(Json.obj().put("algo", algo).put("name", Catalog.name(algo))
                    .raw("counts", counted.countsJson()).put("timeNs", average(times))
                    .raw("theory", Catalog.get(algo)).end());
        }
        rows.append(']');
        return Json.obj().put("category", "compare").put("n", input.length).put("trials", trials)
                .raw("rows", rows.toString()).end();
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
                .put("timeNs", average(times)).put("trials", TIMING_TRIALS)
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
        String note = null;
        int stepLimit = 2500;
        final DynamicArray<String>[] perms = newPermHolder();

        switch (algo) {
            case "factorial": {
                final int n = factorialArg(arg);
                job = t -> n + "! = " + Recursion.factorial(n, t) + " (recursive)";
                break;
            }
            case "factorial-iter": {
                final int n = factorialArg(arg);
                job = t -> n + "! = " + Recursion.factorialIterative(n, t) + " (iterative)";
                break;
            }
            case "fib-naive": {
                final int n = Input.parseInt(arg, "n");
                if (n < 0) throw new IllegalArgumentException("n must be 0 or more: Fibonacci numbers are not defined for negative n.");
                if (n > 30) throw new IllegalArgumentException("Naive recursion is limited to n ≤ 30 (you entered " + n + "). "
                        + "Each call makes two more calls and the same values are recomputed again and again, so the number of calls grows like 1.618ⁿ: "
                        + "fib(30) already needs 2,692,537 calls and every extra 1 in n multiplies the work by about 1.6. Use the memoised or bottom-up version for larger n.");
                job = t -> "fib(" + n + ") = " + Recursion.fibNaive(n, t);
                note = "Naive recursion is limited to n ≤ 30 because it recomputes the same sub-problems: the number of calls is 2·fib(n+1) − 1, "
                        + "which grows like 1.618ⁿ. Here it made " + (2 * Recursion.fibBottomUp(n + 1, Tracer.silent()) - 1) + " call(s) for n = " + n + ".";
                break;
            }
            case "fib-memo": {
                final int n = Input.intInRange(arg, "n", 0, 92);
                job = t -> "fib(" + n + ") = " + Recursion.fibMemo(n, t);
                break;
            }
            case "fib-dp": {
                final int n = Input.intInRange(arg, "n", 0, 92);
                job = t -> "fib(" + n + ") = " + Recursion.fibBottomUp(n, t);
                break;
            }
            case "hanoi": {
                final int n = Input.intInRange(arg, "number of disks", 1, 10);
                final int from = peg(p.getOr("from", "A"), "source");
                final int to = peg(p.getOr("to", "C"), "destination");
                final int via = peg(p.getOr("via", "B"), "auxiliary");
                if (from == to || from == via || to == via)
                    throw new IllegalArgumentException("The source, destination and auxiliary pegs must be three different pegs.");
                job = t -> {
                    DynamicArray<String> list = t.rec() ? new DynamicArray<String>() : null;
                    long moves = Recursion.hanoi(n, from, to, via, list, t);
                    if (list != null) perms[0] = list;
                    long expected = (1L << n) - 1;
                    return moves + " moves for " + n + " disk(s) from peg " + "ABC".charAt(from) + " to peg " + "ABC".charAt(to)
                            + ". Check: 2^" + n + " − 1 = " + expected + (moves == expected ? " ✓" : " ✗");
                };
                extraKey = "moves";
                stepLimit = 4000;
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

        Tracer traced = new Tracer(true, stepLimit);
        String result = job.run(traced);

        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            Tracer silent = Tracer.silent();
            long start = System.nanoTime();
            job.run(silent);
            times[t] = System.nanoTime() - start;
        }

        Json j = Json.obj().put("category", "recursion").put("algo", algo).put("arg", arg)
                .raw("theory", Catalog.get(algo)).put("result", result).put("note", note)
                .raw("counts", traced.countsJson())
                .put("timeNs", average(times)).put("trials", TIMING_TRIALS)
                .put("traceOn", true).put("truncated", traced.truncated())
                .raw("steps", traced.stepsJson());
        if (extraKey != null && perms[0] != null) j.raw(extraKey, Recursion.stringsJson(perms[0], 5040));
        return j.end();
    }

    /** n for both factorials: 0 is allowed (0! = 1), negatives are rejected, 21! would overflow 64 bits. */
    private static int factorialArg(String arg) {
        int n = Input.parseInt(arg, "n");
        if (n < 0) throw new IllegalArgumentException("Factorial is not defined for negative numbers (you entered " + n + "). Enter a whole number from 0 to 20.");
        if (n > 20) throw new IllegalArgumentException("n must be at most 20: 21! is too large for a 64-bit number (you entered " + n + ").");
        return n;
    }

    /** Peg letter A, B or C to its index 0, 1 or 2. */
    private static int peg(String s, String what) {
        String v = s.trim();
        if (v.equals("A") || v.equals("a")) return 0;
        if (v.equals("B") || v.equals("b")) return 1;
        if (v.equals("C") || v.equals("c")) return 2;
        throw new IllegalArgumentException("The " + what + " peg must be A, B or C.");
    }

    @SuppressWarnings("unchecked")
    private static DynamicArray<String>[] newPermHolder() { return (DynamicArray<String>[]) new DynamicArray[1]; }

    // ------------------------------------------------------------------ /run/ds
    public static String ds(Params p) {
        String kind = p.require("kind", "a data structure");
        if (!oneOf(kind, DataStructures.KINDS)) throw new IllegalArgumentException("Unknown data structure: " + kind);
        String script = p.require("script", kind.equals("brackets") || kind.equals("expression") ? "some text" : "at least one command");

        int capacity = 5;
        if (kind.equals("stack") || kind.equals("queue") || kind.equals("linear-queue"))
            capacity = Input.intInRange(p.getOr("capacity", "5"), "Capacity", 1, 20);

        DataStructures traced = new DataStructures(true, capacity);
        traced.run(kind, script);

        long[] times = new long[TIMING_TRIALS];
        for (int t = 0; t < TIMING_TRIALS; t++) {
            DataStructures silent = new DataStructures(false, capacity);
            long start = System.nanoTime();
            silent.run(kind, script);
            times[t] = System.nanoTime() - start;
        }

        String counts = Json.obj().put("total", traced.totalOps).put("operations", traced.stepCount()).end();
        return Json.obj().put("category", "ds").put("algo", kind)
                .raw("theory", Catalog.get("ds-" + kind)).put("result", traced.summary)
                .raw("counts", counts)
                .put("timeNs", average(times)).put("trials", TIMING_TRIALS)
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
        int trials = Input.intInRange(p.getOr("trials", "3"), "timed runs", 3, 7);
        return Analyzer.run(algos, sizes, input, trials);
    }
}
