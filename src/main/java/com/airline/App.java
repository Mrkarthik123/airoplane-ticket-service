package com.airline;

import com.airline.model.*;
import com.airline.service.*;
import com.airline.store.DataStore;
import com.airline.util.*;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Airoplane Ticket Service — zero-dependency Java backend + static UI.
 * Run:  ./run.sh   (or)  java -cp target/classes com.airline.App
 * App:  http://localhost:8080
 */
public class App {
    private final DataStore db;
    private final AuthService auth;
    private final FlightService flights;
    private final BookingService bookings;

    public App(DataStore db) {
        this.db = db;
        this.auth = new AuthService(db);
        this.flights = new FlightService(db);
        this.bookings = new BookingService(db);
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        for (int i = 0; i < args.length - 1; i++) if (args[i].equals("--port")) port = Integer.parseInt(args[i + 1]);
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) { try { port = Integer.parseInt(portEnv.trim()); } catch (Exception ignored) {} }

        Path dataDir = Paths.get("data");
        // when running from jar/classic layout, keep data next to working dir
        DataStore db = new DataStore(dataDir);
        db.load();
        App app = new App(db);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/", app::handleApi);
        server.createContext("/", app::handleStatic);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("✈  Airoplane Ticket Service running at http://localhost:" + port);
        System.out.println("   Demo login: demo@skyjet.in / demo123   |   Admin: admin@skyjet.in / admin123");
    }

    // ---------------- static files ----------------
    private void handleStatic(HttpExchange ex) throws IOException {
        try {
            if (!ex.getRequestMethod().equalsIgnoreCase("GET")) { send(ex, 405, "Method not allowed", "text/plain"); return; }
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            String res = "static" + path;
            byte[] bytes = loadStatic(res);
            if (bytes == null) { // SPA fallback
                bytes = loadStatic("static/index.html");
                if (bytes == null) { send(ex, 404, "Not found", "text/plain"); return; }
                path = "/index.html";
            }
            String ct = "application/octet-stream";
            if (path.endsWith(".html")) ct = "text/html";
            else if (path.endsWith(".css")) ct = "text/css";
            else if (path.endsWith(".js")) ct = "text/javascript";
            else if (path.endsWith(".json")) ct = "application/json";
            else if (path.endsWith(".webmanifest")) ct = "application/manifest+json";
            else if (path.endsWith(".png")) ct = "image/png";
            else if (path.endsWith(".svg")) ct = "image/svg+xml";
            ex.getResponseHeaders().set("Content-Type", ct + "; charset=utf-8");
            ex.sendResponseHeaders(200, bytes.length);
            try (var os = ex.getResponseBody()) { os.write(bytes); }
        } catch (Exception e) {
            send(ex, 500, "Server error: " + e.getMessage(), "text/plain");
        }
    }

    private byte[] loadStatic(String res) {
        try {
            // 1) classpath (mvn / jar)
            try (var in = App.class.getClassLoader().getResourceAsStream(res)) {
                if (in != null) return in.readAllBytes();
            }
            // 2) file system (javac run.sh layout)
            Path p = Paths.get("src/main/resources/" + res);
            if (Files.exists(p)) return Files.readAllBytes(p);
        } catch (Exception ignored) {}
        return null;
    }

    // ---------------- API ----------------
    private void handleApi(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod().toUpperCase();
            String path = ex.getRequestURI().getPath();
            Map<String, String> q = HttpUtil.query(ex.getRequestURI().getRawQuery());

            // CORS preflight
            if (method.equals("OPTIONS")) {
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                ex.sendResponseHeaders(204, -1);
                return;
            }

            // auth
            if (path.equals("/api/auth/register") && method.equals("POST")) {
                Map<String, Object> b = Json.parseObject(HttpUtil.readBody(ex));
                AuthService.AuthResult r = auth.register(Json.str(b, "name", ""), Json.str(b, "email", ""), Json.str(b, "password", ""));
                if (r.error != null) { json(ex, 400, err(r.error)); return; }
                json(ex, 200, ok(Map.of("user", r.user.publicJson(), "token", r.token))); return;
            }
            if (path.equals("/api/auth/login") && method.equals("POST")) {
                Map<String, Object> b = Json.parseObject(HttpUtil.readBody(ex));
                AuthService.AuthResult r = auth.login(Json.str(b, "email", ""), Json.str(b, "password", ""));
                if (r.error != null) { json(ex, 401, err(r.error)); return; }
                json(ex, 200, ok(Map.of("user", r.user.publicJson(), "token", r.token))); return;
            }
            if (path.equals("/api/auth/me") && method.equals("GET")) {
                User u = auth.me(HttpUtil.bearer(ex));
                if (u == null) { json(ex, 401, err("Not logged in.")); return; }
                json(ex, 200, ok(Map.of("user", u.publicJson()))); return;
            }

            // flights search
            if (path.equals("/api/flights") && method.equals("GET")) {
                List<Object> list = new ArrayList<>();
                for (Flight f : flights.search(q.get("from"), q.get("to"), q.get("date"))) list.add(f.toJson());
                Map<String, Object> airports = new LinkedHashMap<>();
                airports.put("DEL", "New Delhi (DEL)"); airports.put("BOM", "Mumbai (BOM)");
                airports.put("BLR", "Bengaluru (BLR)"); airports.put("HYD", "Hyderabad (HYD)");
                airports.put("MAA", "Chennai (MAA)");
                json(ex, 200, ok(Map.of("flights", list, "airports", airports))); return;
            }
            if (path.equals("/api/flights") && method.equals("POST")) {
                User u = requireAdmin(ex); if (u == null) return;
                Flight f = flights.create(Json.parseObject(HttpUtil.readBody(ex)));
                json(ex, 201, ok(Map.of("flight", f.toJson()))); return;
            }
            if (path.startsWith("/api/flights/")) {
                String id = path.substring("/api/flights/".length());
                if (method.equals("GET")) {
                    Flight f = flights.get(id);
                    if (f == null) { json(ex, 404, err("Flight not found.")); return; }
                    json(ex, 200, ok(Map.of("flight", f.toJson()))); return;
                }
                if (method.equals("PUT")) {
                    User u = requireAdmin(ex); if (u == null) return;
                    Flight f = flights.update(id, Json.parseObject(HttpUtil.readBody(ex)));
                    if (f == null) { json(ex, 404, err("Flight not found.")); return; }
                    json(ex, 200, ok(Map.of("flight", f.toJson()))); return;
                }
                if (method.equals("DELETE")) {
                    User u = requireAdmin(ex); if (u == null) return;
                    boolean okDel = flights.delete(id);
                    if (!okDel) { json(ex, 400, err("Cannot delete: flight has active bookings or does not exist.")); return; }
                    json(ex, 200, ok(Map.of("deleted", true))); return;
                }
            }

            // mock payment
            if (path.equals("/api/payments/mock") && method.equals("POST")) {
                Map<String, Object> b = Json.parseObject(HttpUtil.readBody(ex));
                String card = Json.str(b, "cardNumber", "").replaceAll("\\s", "");
                if (!card.matches("\\d{12,19}")) { json(ex, 400, err("Enter a 12–19 digit test card number (e.g. 4111 1111 1111 1111).")); return; }
                double amount = Json.dbl(b, "amount", 0);
                Map<String, Object> pay = bookings.mockPay(amount, card.substring(card.length() - 4));
                json(ex, 200, ok(Map.of("payment", pay))); return;
            }

            // bookings
            if (path.equals("/api/bookings") && method.equals("POST")) {
                User u = requireLogin(ex); if (u == null) return;
                Map<String, Object> b = Json.parseObject(HttpUtil.readBody(ex));
                String flightId = Json.str(b, "flightId", "");
                List<Map<String, Object>> pax = new ArrayList<>();
                Object po = b.get("passengers");
                if (po instanceof List) for (Object o : (List<Object>) po) if (o instanceof Map) pax.add((Map<String, Object>) o);
                List<String> seats = new ArrayList<>();
                Object so = b.get("seats");
                if (so instanceof List) for (Object o : (List<Object>) so) seats.add(String.valueOf(o));
                BookingService.Result r = bookings.create(u.id, flightId, pax, seats, Json.str(b, "paymentId", ""), Json.dbl(b, "paidAmount", 0));
                if (r.error != null) { json(ex, 400, err(r.error)); return; }
                json(ex, 201, ok(Map.of("booking", enrich(r.booking)))); return;
            }
            if (path.equals("/api/bookings") && method.equals("GET")) {
                User u = requireLogin(ex); if (u == null) return;
                List<Object> list = new ArrayList<>();
                List<Booking> src = u.role.equals("admin") ? bookings.all() : bookings.forUser(u.id);
                for (Booking bl : src) list.add(enrich(bl));
                json(ex, 200, ok(Map.of("bookings", list))); return;
            }
            if (path.startsWith("/api/bookings/") && path.endsWith("/cancel") && method.equals("POST")) {
                User u = requireLogin(ex); if (u == null) return;
                String pnr = path.substring("/api/bookings/".length(), path.length() - "/cancel".length());
                BookingService.Result r = bookings.cancel(pnr, u.id, u.role.equals("admin"));
                if (r.error != null) { json(ex, 400, err(r.error)); return; }
                json(ex, 200, ok(Map.of("booking", enrich(r.booking)))); return;
            }
            if (path.startsWith("/api/bookings/") && method.equals("GET")) {
                User u = requireLogin(ex); if (u == null) return;
                String pnr = path.substring("/api/bookings/".length());
                Booking bl = db.bookings.get(pnr);
                if (bl == null) { json(ex, 404, err("Booking not found.")); return; }
                if (!u.role.equals("admin") && !bl.userId.equals(u.id)) { json(ex, 403, err("Not your booking.")); return; }
                json(ex, 200, ok(Map.of("booking", enrich(bl)))); return;
            }

            // admin stats
            if (path.equals("/api/admin/stats") && method.equals("GET")) {
                User u = requireAdmin(ex); if (u == null) return;
                double revenue = 0; int active = 0;
                for (Booking bl : db.bookings.values()) if (bl.status.equals("CONFIRMED")) { revenue += bl.totalPrice; active++; }
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("flights", db.flights.size()); s.put("users", db.users.size());
                s.put("bookings", db.bookings.size()); s.put("activeBookings", active);
                s.put("revenue", revenue);
                json(ex, 200, ok(Map.of("stats", s))); return;
            }

            json(ex, 404, err("Unknown endpoint: " + path));
        } catch (Exception e) {
            json(ex, 500, err("Server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> enrich(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>(b.toJson());
        Flight f = db.flights.get(b.flightId);
        if (f != null) m.put("flight", f.toJson());
        User u = db.users.get(b.userId);
        if (u != null) m.put("customer", u.publicJson());
        return m;
    }

    private User requireLogin(HttpExchange ex) throws IOException {
        User u = auth.me(HttpUtil.bearer(ex));
        if (u == null) json(ex, 401, err("Login required."));
        return u;
    }

    private User requireAdmin(HttpExchange ex) throws IOException {
        User u = auth.me(HttpUtil.bearer(ex));
        if (u == null) { json(ex, 401, err("Login required.")); return null; }
        if (!u.role.equals("admin")) { json(ex, 403, err("Admin only.")); return null; }
        return u;
    }

    private static Map<String, Object> ok(Map<String, Object> d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true); m.putAll(d);
        return m;
    }

    private static Map<String, Object> err(String msg) {
        return Map.of("ok", false, "error", msg);
    }

    private void json(HttpExchange ex, int status, Map<String, Object> m) throws IOException {
        HttpUtil.sendJson(ex, status, Json.stringify(m));
    }

    private void send(HttpExchange ex, int status, String text, String ct) throws IOException {
        HttpUtil.sendText(ex, status, text, ct);
    }
}
