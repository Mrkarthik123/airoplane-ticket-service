package com.airline.util;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** HTTP helpers: read body, query params, send JSON/text. */
public final class HttpUtil {
    private HttpUtil() {}

    public static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> query(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null || raw.isEmpty()) return m;
        for (String part : raw.split("&")) {
            int eq = part.indexOf('=');
            try {
                if (eq > 0) m.put(URLDecoder.decode(part.substring(0, eq), "UTF-8"),
                        URLDecoder.decode(part.substring(eq + 1), "UTF-8"));
                else if (!part.isEmpty()) m.put(URLDecoder.decode(part, "UTF-8"), "");
            } catch (Exception ignored) {}
        }
        return m;
    }

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    public static void sendText(HttpExchange ex, int status, String text, String contentType) throws IOException {
        byte[] b = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    public static String bearer(HttpExchange ex) {
        String h = ex.getRequestHeaders().getFirst("Authorization");
        if (h != null && h.startsWith("Bearer ")) return h.substring(7).trim();
        return "";
    }
}
