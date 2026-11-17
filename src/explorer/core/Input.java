package explorer.core;

import explorer.ds.DynamicArray;
import explorer.ds.IntList;

/** Hand-written parsing of the text the user types. */
public final class Input {
    private Input() {}

    private static boolean isSeparator(char c) {
        return c == ',' || c == ';' || c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    /** Splits text on commas, semicolons and whitespace. Empty tokens are skipped. */
    public static DynamicArray<String> tokens(String text) {
        DynamicArray<String> out = new DynamicArray<String>();
        if (text == null) return out;
        int i = 0, n = text.length();
        while (i < n) {
            while (i < n && isSeparator(text.charAt(i))) i++;
            int start = i;
            while (i < n && !isSeparator(text.charAt(i))) i++;
            if (i > start) out.add(text.substring(start, i));
        }
        return out;
    }

    /** Parses one integer by hand, with a clear message if it is not valid. */
    public static int parseInt(String s, String what) {
        if (s == null || s.length() == 0) throw new IllegalArgumentException("Enter a value for " + what + ".");
        int i = 0;
        boolean neg = false;
        if (s.charAt(0) == '-' || s.charAt(0) == '+') { neg = s.charAt(0) == '-'; i = 1; }
        if (i == s.length()) throw new IllegalArgumentException("\"" + s + "\" is not a whole number (" + what + ").");
        long v = 0;
        for (; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9')
                throw new IllegalArgumentException("\"" + s + "\" is not a whole number (" + what + ").");
            v = v * 10 + (c - '0');
            if (v > 1_000_000_000L)
                throw new IllegalArgumentException("\"" + s + "\" is too large; use values between -1,000,000,000 and 1,000,000,000.");
        }
        return (int) (neg ? -v : v);
    }

    /** Parses a list of integers such as "5, 3 8;1". */
    public static int[] parseInts(String text, int maxCount) {
        DynamicArray<String> toks = tokens(text);
        if (toks.size() == 0) throw new IllegalArgumentException("Enter at least one number, separated by commas or spaces.");
        if (toks.size() > maxCount)
            throw new IllegalArgumentException("You entered " + toks.size() + " numbers; the limit here is " + maxCount + ".");
        IntList out = new IntList();
        for (int i = 0; i < toks.size(); i++) out.add(parseInt(toks.get(i), "item " + (i + 1)));
        return out.toArray();
    }

    /** Reads an int and checks it lies in [lo, hi]. */
    public static int intInRange(String s, String what, int lo, int hi) {
        int v = parseInt(s == null ? null : s.trim(), what);
        if (v < lo || v > hi)
            throw new IllegalArgumentException(what + " must be between " + lo + " and " + hi + " (you entered " + v + ").");
        return v;
    }

    public static int[] copy(int[] a) {
        int[] c = new int[a.length];
        for (int i = 0; i < a.length; i++) c[i] = a[i];
        return c;
    }
}
