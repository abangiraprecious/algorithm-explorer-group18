package explorer.algorithms;

import explorer.core.Json;
import explorer.core.Tracer;
import explorer.ds.ArrayStack;
import explorer.ds.DynamicArray;

/**
 * Recursive algorithms. Every call goes through Tracer.enter/exit so the interface can
 * show the call stack at each step and draw the recursion tree.
 *
 * The result of each run is returned as a String (numbers can be large; some results are lists).
 */
public final class Recursion {
    private Recursion() {}

    // ---------------- Factorial: T(n) = T(n-1) + O(1)  →  O(n) time, O(n) stack ----------------
    public static long factorial(int n, Tracer t) {
        String label = !t.rec() ? null : "fact(" + n + ")";
        int id = t.enter(label);
        long r;
        t.comparisons++;
        if (n <= 1) {
            if (t.on()) t.note("base", "Base case: " + n + "! = 1", null, null);
            r = 1;
        } else {
            r = n * factorial(n - 1, t);
            t.arith++;
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    // ---------------- Factorial, iterative: one loop, n − 1 iterations  →  O(n) time, O(1) space ----------------
    public static long factorialIterative(int n, Tracer t) {
        long r = 1;
        if (n <= 1 && t.on()) t.note("base", n + "! = 1 (the loop body never runs)", null, null);
        for (int i = 2; i <= n; i++) {
            t.iterations++;
            r = r * i;
            t.arith++;
            if (t.on()) t.note("loop", "i = " + i + ": result = " + (r / i) + " × " + i + " = " + r, null, null);
        }
        return r;
    }

    // ---------------- Fibonacci, naive: T(n) = T(n-1) + T(n-2) + O(1)  →  O(φ^n) ≈ O(2^n) ----------------
    public static long fibNaive(int n, Tracer t) {
        String label = !t.rec() ? null : "fib(" + n + ")";
        int id = t.enter(label);
        long r;
        t.comparisons++;
        if (n <= 1) {
            r = n;
        } else {
            r = fibNaive(n - 1, t) + fibNaive(n - 2, t);
            t.arith++;
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    // ---------------- Fibonacci, memoised (top-down dynamic programming): O(n) ----------------
    public static long fibMemo(int n, Tracer t) {
        long[] memo = new long[n + 1];
        for (int i = 0; i <= n; i++) memo[i] = -1;
        return fibMemo(n, memo, t);
    }

    /** Memoised fib(n): returns memo[n] if it is already known, otherwise computes it once and stores it. O(n) overall. */
    private static long fibMemo(int n, long[] memo, Tracer t) {
        String label = !t.rec() ? null : "fib(" + n + ")";
        int id = t.enter(label);
        long r;
        t.comparisons++;
        if (memo[n] != -1) {
            if (t.on()) t.note("memo", "fib(" + n + ") was already computed: reuse " + memo[n] + " instead of recursing", null, null);
            r = memo[n];
        } else if (n <= 1) {
            r = n;
            memo[n] = r;
            t.writes++;
        } else {
            r = fibMemo(n - 1, memo, t) + fibMemo(n - 2, memo, t);
            t.arith++;
            memo[n] = r;
            t.writes++;
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    // ---------------- Fibonacci, bottom-up dynamic programming: fill table[0..n] from the bottom  →  O(n) time, O(n) space ----------------
    public static long fibBottomUp(int n, Tracer t) {
        long[] table = new long[n + 2];
        table[0] = 0;
        table[1] = 1;
        t.writes += 2;
        if (t.on()) t.note("info", "table[0] = 0 and table[1] = 1 are the base cases", null, null);
        for (int i = 2; i <= n; i++) {
            t.iterations++;
            table[i] = table[i - 1] + table[i - 2];
            t.arith++;
            t.writes++;
            if (t.on()) t.note("loop", "i = " + i + ": table[" + i + "] = table[" + (i - 1) + "] + table[" + (i - 2) + "] = "
                    + table[i - 1] + " + " + table[i - 2] + " = " + table[i], null, null);
        }
        return table[n];
    }

    // ---------------- Tower of Hanoi: T(n) = 2T(n-1) + 1  →  2^n − 1 moves ----------------
    private static final String[] PEG = {"A", "B", "C"};

    /**
     * Solves Hanoi for n disks from peg `from` to peg `to` using peg `via` (0 = A, 1 = B, 2 = C).
     * Every move is added to `moveList` (when it is not null). Returns the number of moves. O(2^n).
     */
    public static long hanoi(int n, int from, int to, int via, DynamicArray<String> moveList, Tracer t) {
        DynamicArray<ArrayStack<Integer>> pegs = new DynamicArray<ArrayStack<Integer>>(3);
        for (int i = 0; i < 3; i++) pegs.add(new ArrayStack<Integer>());
        for (int d = n; d >= 1; d--) pegs.get(from).push(d);
        long[] moves = new long[1];
        if (t.on()) t.note("info", "Start: " + n + " disks on peg " + PEG[from] + ". Goal: move them all to peg " + PEG[to]
                + " using peg " + PEG[via] + " as the auxiliary.", "pegs", pegsJson(pegs));
        hanoi(n, from, to, via, pegs, moves, moveList, t);
        if (t.on()) t.note("done", "Finished: all " + n + " disks are on peg " + PEG[to] + " after " + moves[0]
                + " moves, and 2^" + n + " − 1 = " + ((1L << n) - 1), "pegs", pegsJson(pegs));
        return moves[0];
    }

    /** Default puzzle: from A to C using B. */
    public static long hanoi(int n, Tracer t) { return hanoi(n, 0, 2, 1, null, t); }

    private static void hanoi(int n, int from, int to, int via, DynamicArray<ArrayStack<Integer>> pegs,
                              long[] moves, DynamicArray<String> moveList, Tracer t) {
        String label = !t.rec() ? null : "hanoi(" + n + ", " + PEG[from] + "→" + PEG[to] + ")";
        int id = t.enter(label);
        t.comparisons++;
        if (n == 1) {
            move(from, to, pegs, moves, moveList, t);
        } else {
            hanoi(n - 1, from, via, to, pegs, moves, moveList, t);   // clear the n−1 smaller disks onto the auxiliary peg
            move(from, to, pegs, moves, moveList, t);                // move the largest disk
            hanoi(n - 1, via, to, from, pegs, moves, moveList, t);   // put the n−1 disks back on top of it
        }
        t.exit(id, label, "done");
    }

    private static void move(int from, int to, DynamicArray<ArrayStack<Integer>> pegs, long[] moves,
                             DynamicArray<String> moveList, Tracer t) {
        int disk = pegs.get(from).pop();
        pegs.get(to).push(disk);
        moves[0]++;
        t.writes++;
        if (moveList != null) moveList.add("Move disk " + disk + " from " + PEG[from] + " to " + PEG[to]);
        if (t.on()) t.note("move", "Move " + moves[0] + ": disk " + disk + " from " + PEG[from] + " to " + PEG[to],
                "pegs", pegsJson(pegs));
    }

    /** The three pegs as JSON, bottom disk first. O(n) for n disks. */
    private static String pegsJson(DynamicArray<ArrayStack<Integer>> pegs) {
        StringBuilder sb = new StringBuilder("[");
        for (int p = 0; p < 3; p++) {
            if (p > 0) sb.append(',');
            sb.append('[');
            ArrayStack<Integer> s = pegs.get(p);
            for (int i = 0; i < s.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(s.fromBottom(i));
            }
            sb.append(']');
        }
        return sb.append(']').toString();
    }

    // ---------------- Fast power (exponentiation by squaring): T(n) = T(n/2) + O(1)  →  O(log n) ----------------
    public static long power(long x, int n, Tracer t) {
        String label = !t.rec() ? null : "pow(" + x + ", " + n + ")";
        int id = t.enter(label);
        long r;
        t.comparisons++;
        if (n == 0) {
            r = 1;
        } else {
            long half = power(x, n / 2, t);
            t.arith++;                       // n / 2
            r = mulChecked(half, half);
            t.arith++;
            t.comparisons++;
            if (n % 2 == 1) {
                r = mulChecked(r, x);
                t.arith++;
                if (t.on()) t.note("info", n + " is odd: pow(" + x + "," + n + ") = half² × " + x + " = " + r, null, null);
            } else {
                if (t.on()) t.note("info", n + " is even: pow(" + x + "," + n + ") = half² = " + half + "² = " + r, null, null);
            }
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    /** a × b, with an error instead of a silent 64-bit overflow. O(1). */
    private static long mulChecked(long a, long b) {
        long r = a * b;
        // overflow check without Math.multiplyExact: division must give back the other factor
        if (a != 0 && (r / a != b || (a == -1 && b == Long.MIN_VALUE)))
            throw new IllegalArgumentException("The result is too large for a 64-bit number. Try a smaller base or exponent.");
        return r;
    }

    // ---------------- Euclid's GCD: O(log(min(a, b))) ----------------
    public static long gcd(long a, long b, Tracer t) {
        String label = !t.rec() ? null : "gcd(" + a + ", " + b + ")";
        int id = t.enter(label);
        long r;
        t.comparisons++;
        if (b == 0) {
            r = a;
            if (t.on()) t.note("base", "Base case: b = 0, so the answer is " + a, null, null);
        } else {
            t.arith++;
            r = gcd(b, a % b, t);
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    // ---------------- Permutations by backtracking: O(n · n!) ----------------
    public static DynamicArray<String> permutations(String s, Tracer t) {
        DynamicArray<String> out = new DynamicArray<String>();
        char[] c = s.toCharArray();
        permute(c, 0, out, t);
        return out;
    }

    /** Fixes position k with each remaining character in turn, recurses, then swaps back (backtracking). O(n · n!). */
    private static void permute(char[] c, int k, DynamicArray<String> out, Tracer t) {
        String label = !t.rec() ? null : "perm(\"" + new String(c, 0, k) + "|" + new String(c, k, c.length - k) + "\")";
        int id = t.enter(label);
        t.comparisons++;
        if (k == c.length) {
            out.add(new String(c));
            if (t.on()) t.note("base", "All positions fixed → output \"" + new String(c) + "\"", null, null);
        } else {
            for (int i = k; i < c.length; i++) {
                swapChars(c, k, i, t);
                permute(c, k + 1, out, t);
                swapChars(c, k, i, t);       // backtrack: undo the choice
            }
        }
        t.exit(id, label, t.rec() ? (k == c.length ? "\"" + new String(c) + "\"" : "done") : null);
    }

    /** Exchanges c[i] and c[j] and counts one swap. O(1). */
    private static void swapChars(char[] c, int i, int j, Tracer t) {
        char tmp = c[i]; c[i] = c[j]; c[j] = tmp;
        t.swaps++;
    }

    // ---------------- Palindrome check: O(n) time, O(n) stack ----------------
    public static boolean palindrome(String s, Tracer t) {
        return pal(s, 0, s.length() - 1, t);
    }

    /** True if s[lo..hi] is a palindrome: compare the outer pair, then recurse on the inside. O(n). */
    private static boolean pal(String s, int lo, int hi, Tracer t) {
        String label = !t.rec() ? null : "isPal(" + lo + ", " + hi + ")";
        int id = t.enter(label);
        boolean r;
        if (lo >= hi) {
            r = true;
            if (t.on()) t.note("base", "Base case: nothing left to compare", null, null);
        } else {
            t.comparisons++;
            char a = s.charAt(lo), b = s.charAt(hi);
            if (a != b) {
                r = false;
                if (t.on()) t.note("base", "'" + a + "' ≠ '" + b + "': not a palindrome", null, null);
            } else {
                if (t.on()) t.note("info", "'" + a + "' = '" + b + "': check the inside", null, null);
                r = pal(s, lo + 1, hi - 1, t);
            }
        }
        t.exit(id, label, t.rec() ? String.valueOf(r) : null);
        return r;
    }

    /** Small helper used by the API layer to build a JSON list of strings. */
    public static String stringsJson(DynamicArray<String> list, int max) {
        StringBuilder sb = new StringBuilder("[");
        int n = list.size() < max ? list.size() : max;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(',');
            sb.append(Json.quote(list.get(i)));
        }
        return sb.append(']').toString();
    }
}
