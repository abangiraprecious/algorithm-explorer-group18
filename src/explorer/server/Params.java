package explorer.server;

import explorer.ds.HashTable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Parses "a=1&b=two" form data into our own HashTable. */
public final class Params {
    private final HashTable<String> map = new HashTable<String>(16);

    public Params(String encoded) {
        if (encoded == null || encoded.length() == 0) return;
        int start = 0;
        for (int i = 0; i <= encoded.length(); i++) {
            if (i == encoded.length() || encoded.charAt(i) == '&') {
                String pair = encoded.substring(start, i);
                start = i + 1;
                if (pair.length() == 0) continue;
                int eq = pair.indexOf('=');
                String k = eq < 0 ? pair : pair.substring(0, eq);
                String v = eq < 0 ? "" : pair.substring(eq + 1);
                map.put(decode(k), decode(v));
            }
        }
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    public String get(String key) { return map.get(key); }

    public String require(String key, String what) {
        String v = map.get(key);
        if (v == null || v.trim().length() == 0) throw new IllegalArgumentException("Enter " + what + ".");
        return v;
    }

    public String getOr(String key, String fallback) {
        String v = map.get(key);
        return v == null || v.length() == 0 ? fallback : v;
    }
}
