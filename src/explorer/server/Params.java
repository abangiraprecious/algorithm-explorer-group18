package explorer.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple query parameter parser for ?action=...&input=...
 */
public class Params {

    private final Map<String, String> map = new HashMap<>();

    public Params(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String value = decode(pair.substring(idx + 1));
                map.put(key, value);
            } else if (!pair.isEmpty()) {
                map.put(decode(pair), "");
            }
        }
    }

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public String get(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    public String get(String key) {
        return map.get(key);
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }
}
