package explorer.core;

/**
 * Minimal JSON writer built on StringBuilder (no JSON library).
 * Usage: Json.obj().put("name", "x").put("n", 3).end()
 */
public final class Json {
    private final StringBuilder sb = new StringBuilder();
    private boolean first = true;

    private Json() {}

    public static Json obj() {
        Json j = new Json();
        j.sb.append('{');
        return j;
    }

    private void key(String k) {
        if (!first) sb.append(',');
        first = false;
        quote(sb, k);
        sb.append(':');
    }

    public Json put(String k, String v) {
        key(k);
        if (v == null) sb.append("null"); else quote(sb, v);
        return this;
    }

    public Json put(String k, long v) { key(k); sb.append(v); return this; }

    public Json put(String k, int v) { key(k); sb.append(v); return this; }

    public Json put(String k, boolean v) { key(k); sb.append(v); return this; }

    public Json put(String k, double v) {
        key(k);
        if (Double.isNaN(v) || Double.isInfinite(v)) sb.append("null");
        else sb.append(v);
        return this;
    }

    public Json put(String k, int[] arr) { key(k); sb.append(arr(arr)); return this; }

    /** Inserts already-serialised JSON (an object, array, number...). */
    public Json raw(String k, String json) { key(k); sb.append(json == null ? "null" : json); return this; }

    public String end() {
        sb.append('}');
        return sb.toString();
    }

    public static String arr(int[] a) {
        if (a == null) return "null";
        StringBuilder s = new StringBuilder(a.length * 4 + 2);
        s.append('[');
        for (int i = 0; i < a.length; i++) {
            if (i > 0) s.append(',');
            s.append(a[i]);
        }
        return s.append(']').toString();
    }

    /** Joins already-serialised JSON values into a JSON array. */
    public static String rawArr(String[] items, int count) {
        StringBuilder s = new StringBuilder();
        s.append('[');
        for (int i = 0; i < count; i++) {
            if (i > 0) s.append(',');
            s.append(items[i]);
        }
        return s.append(']').toString();
    }

    public static String quote(String s) {
        StringBuilder b = new StringBuilder(s.length() + 2);
        quote(b, s);
        return b.toString();
    }

    private static void quote(StringBuilder b, String s) {
        b.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        b.append("\\u");
                        for (int k = hex.length(); k < 4; k++) b.append('0');
                        b.append(hex);
                    } else b.append(c);
            }
        }
        b.append('"');
    }
}
