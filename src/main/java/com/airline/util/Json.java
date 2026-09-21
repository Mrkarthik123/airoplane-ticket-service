package com.airline.util;

import java.util.*;

/**
 * Minimal JSON parser + serializer (no external deps).
 * Supports objects, arrays, strings, numbers, booleans, null.
 */
public final class Json {
    private Json() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String s) {
        Object v = new Parser(s).parseValue();
        if (v instanceof Map) return (Map<String, Object>) v;
        return new LinkedHashMap<>();
    }

    public static List<Object> parseArray(String s) {
        Object v = new Parser(s).parseValue();
        if (v instanceof List) return (List<Object>) v;
        return new ArrayList<>();
    }

    public static Object parse(String s) {
        return new Parser(s).parseValue();
    }

    public static String stringify(Object v) {
        if (v == null) return "null";
        if (v instanceof String) return quote((String) v);
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) sb.append(",");
                sb.append(quote(String.valueOf(e.getKey()))).append(":").append(stringify(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        }
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object o : (Collection<?>) v) {
                if (!first) sb.append(",");
                sb.append(stringify(o));
                first = false;
            }
            return sb.append("]").toString();
        }
        return quote(String.valueOf(v));
    }

    public static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.append("\"").toString();
    }

    public static String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v == null ? def : String.valueOf(v);
    }

    public static int num(Map<String, Object> m, String key, int def) {
        try {
            Object v = m.get(key);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v != null) return Integer.parseInt(String.valueOf(v));
        } catch (Exception ignored) {}
        return def;
    }

    public static double dbl(Map<String, Object> m, String key, double def) {
        try {
            Object v = m.get(key);
            if (v instanceof Number) return ((Number) v).doubleValue();
            if (v != null) return Double.parseDouble(String.valueOf(v));
        } catch (Exception ignored) {}
        return def;
    }

    private static final class Parser {
        private final String s;
        private int i;
        Parser(String s) { this.s = s == null ? "" : s.trim(); }
        private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        Object parseValue() {
            ws();
            if (i >= s.length()) return null;
            char c = s.charAt(i);
            if (c == '{') return parseObj();
            if (c == '[') return parseArr();
            if (c == '"') return parseStr();
            if (c == 't' && s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (c == 'f' && s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (c == 'n' && s.startsWith("null", i)) { i += 4; return null; }
            return parseNum();
        }
        private Map<String, Object> parseObj() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; ws();
            if (i < s.length() && s.charAt(i) == '}') { i++; return m; }
            while (i < s.length()) {
                ws();
                String k = parseStr();
                ws();
                if (i < s.length() && s.charAt(i) == ':') i++;
                Object v = parseValue();
                m.put(k, v);
                ws();
                if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
                if (i < s.length() && s.charAt(i) == '}') { i++; break; }
                break;
            }
            return m;
        }
        private List<Object> parseArr() {
            List<Object> l = new ArrayList<>();
            i++; ws();
            if (i < s.length() && s.charAt(i) == ']') { i++; return l; }
            while (i < s.length()) {
                l.add(parseValue());
                ws();
                if (i < s.length() && s.charAt(i) == ',') { i++; continue; }
                if (i < s.length() && s.charAt(i) == ']') { i++; break; }
                break;
            }
            return l;
        }
        private String parseStr() {
            if (i < s.length() && s.charAt(i) == '"') i++;
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\' && i < s.length()) {
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (i + 4 <= s.length()) {
                                sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                                i += 4;
                            }
                            break;
                        default: sb.append(e);
                    }
                } else sb.append(c);
            }
            return sb.toString();
        }
        private Number parseNum() {
            int j = i;
            while (j < s.length() && "-+0123456789.eE".indexOf(s.charAt(j)) >= 0) j++;
            String t = s.substring(i, j);
            i = j;
            try {
                if (t.contains(".") || t.contains("e") || t.contains("E")) return Double.parseDouble(t);
                return Long.parseLong(t);
            } catch (Exception e) { return 0; }
        }
    }
}
