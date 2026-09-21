package com.airline.store;

import com.airline.model.*;
import com.airline.util.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-backed data store (data/*.json). Thread-safe via synchronized blocks.
 * Seeds demo Indian domestic flights + admin/customer users on first run.
 */
public class DataStore {
    private final Path dataDir;
    public final Map<String, Flight> flights = new ConcurrentHashMap<>();
    public final Map<String, User> users = new ConcurrentHashMap<>();
    public final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    /** token -> userId */
    public final Map<String, String> sessions = new ConcurrentHashMap<>();

    public DataStore(Path dataDir) {
        this.dataDir = dataDir;
    }

    public synchronized void load() throws IOException {
        Files.createDirectories(dataDir);
        loadFlights(); loadUsers(); loadBookings();
        if (flights.isEmpty()) seedFlights();
        if (users.isEmpty()) seedUsers();
        rollFlightDatesForward();
        saveAll();
    }

    // ---------- flights ----------
    private void loadFlights() throws IOException {
        Path f = dataDir.resolve("flights.json");
        if (!Files.exists(f)) return;
        String s = Files.readString(f, StandardCharsets.UTF_8).trim();
        if (s.isEmpty()) return;
        for (Object o : Json.parseArray(s)) {
            if (o instanceof Map) {
                Flight fl = Flight.fromJson((Map<String, Object>) o);
                flights.put(fl.id, fl);
            }
        }
    }

    // ---------- users ----------
    private void loadUsers() throws IOException {
        Path f = dataDir.resolve("users.json");
        if (!Files.exists(f)) return;
        String s = Files.readString(f, StandardCharsets.UTF_8).trim();
        if (s.isEmpty()) return;
        for (Object o : Json.parseArray(s)) {
            if (o instanceof Map) {
                User u = User.fromJson((Map<String, Object>) o);
                users.put(u.id, u);
            }
        }
    }

    // ---------- bookings ----------
    private void loadBookings() throws IOException {
        Path f = dataDir.resolve("bookings.json");
        if (!Files.exists(f)) return;
        String s = Files.readString(f, StandardCharsets.UTF_8).trim();
        if (s.isEmpty()) return;
        for (Object o : Json.parseArray(s)) {
            if (o instanceof Map) {
                Booking b = Booking.fromJson((Map<String, Object>) o);
                bookings.put(b.pnr, b);
            }
        }
    }

    public synchronized void saveAll() throws IOException {
        List<Object> fl = new ArrayList<>();
        for (Flight x : flights.values()) fl.add(x.toJson());
        Files.writeString(dataDir.resolve("flights.json"), Json.stringify(fl), StandardCharsets.UTF_8);
        List<Object> ul = new ArrayList<>();
        for (User u : users.values()) ul.add(u.toJson(true));
        Files.writeString(dataDir.resolve("users.json"), Json.stringify(ul), StandardCharsets.UTF_8);
        List<Object> bl = new ArrayList<>();
        for (Booking b : bookings.values()) bl.add(b.toJson());
        Files.writeString(dataDir.resolve("bookings.json"), Json.stringify(bl), StandardCharsets.UTF_8);
    }

    public void saveQuietly() {
        try { saveAll(); } catch (Exception ignored) {}
    }

    // ---------- seed ----------
    private void seedUsers() {
        User admin = new User();
        admin.id = "u-admin"; admin.name = "Admin";
        admin.email = "admin@skyjet.in";
        admin.passwordHash = Passwords.hash("admin123");
        admin.role = "admin";
        users.put(admin.id, admin);

        User demo = new User();
        demo.id = "u-demo"; demo.name = "Demo Flyer";
        demo.email = "demo@skyjet.in";
        demo.passwordHash = Passwords.hash("demo123");
        demo.role = "customer";
        users.put(demo.id, demo);
    }

    private void seedFlights() {
        // date rolling: use a fixed demo date + today-ish dates so search always hits
        String d1 = demoDate(1), d2 = demoDate(2), d3 = demoDate(3);
        add(new Flight("f1", "SkyJet Airways", "SJ-101", "DEL", "BOM", d1, "06:00", "08:10", 130, 5499, 180));
        add(new Flight("f2", "SkyJet Airways", "SJ-204", "DEL", "BOM", d1, "14:30", "16:45", 135, 6299, 180));
        add(new Flight("f3", "Air Himalaya", "AH-312", "DEL", "BLR", d1, "09:15", "12:00", 165, 6999, 180));
        add(new Flight("f4", "Deccan Wings", "DW-418", "BOM", "BLR", d2, "07:45", "09:20", 95, 4999, 150));
        add(new Flight("f5", "SkyJet Airways", "SJ-520", "DEL", "HYD", d2, "11:00", "13:10", 130, 5799, 180));
        add(new Flight("f6", "Air Himalaya", "AH-611", "BLR", "DEL", d2, "18:00", "20:45", 165, 7199, 180));
        add(new Flight("f7", "Deccan Wings", "DW-702", "DEL", "MAA", d3, "08:30", "11:15", 165, 6499, 150));
        add(new Flight("f8", "SkyJet Airways", "SJ-808", "BOM", "DEL", d3, "19:20", "21:30", 130, 5899, 180));
        add(new Flight("f9", "Air Himalaya", "AH-909", "HYD", "BOM", d3, "10:05", "11:25", 80, 4299, 150));
        add(new Flight("f10", "Deccan Wings", "DW-515", "BLR", "HYD", d1, "16:40", "17:50", 70, 3599, 120));
        // pre-book a few random seats so seat maps look realistic
        Random r = new Random(42);
        for (Flight f : flights.values()) {
            int n = 8 + r.nextInt(20);
            for (int k = 0; k < n; k++) f.bookedSeats.add(randomSeat(r));
        }
    }

    private void add(Flight f) { flights.put(f.id, f); }

    /**
     * Keep the demo searchable: shift all flight dates forward so the earliest
     * flight is tomorrow. Flight IDs, seats and bookings stay intact.
     */
    private void rollFlightDatesForward() {
        try {
            java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
            java.time.LocalDate earliest = null;
            for (Flight f : flights.values()) {
                try {
                    java.time.LocalDate d = java.time.LocalDate.parse(f.date);
                    if (earliest == null || d.isBefore(earliest)) earliest = d;
                } catch (Exception ignored) {}
            }
            if (earliest == null || !earliest.isBefore(tomorrow)) return;
            long shift = java.time.temporal.ChronoUnit.DAYS.between(earliest, tomorrow);
            for (Flight f : flights.values()) {
                try {
                    f.date = java.time.LocalDate.parse(f.date).plusDays(shift).toString();
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static String demoDate(int plusDays) {
        java.time.LocalDate d = java.time.LocalDate.now().plusDays(plusDays);
        return d.toString();
    }

    private static String randomSeat(Random r) {
        int row = 1 + r.nextInt(30);
        char col = "ABCDEF".charAt(r.nextInt(6));
        return row + String.valueOf(col);
    }

    public User findUserByEmail(String email) {
        for (User u : users.values()) if (u.email.equalsIgnoreCase(email)) return u;
        return null;
    }
}
