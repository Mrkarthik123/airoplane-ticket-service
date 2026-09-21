package com.airline.service;

import com.airline.model.*;
import com.airline.store.DataStore;
import java.time.LocalDateTime;
import java.util.*;

/** Flight search + admin CRUD. */
public class FlightService {
    private final DataStore db;
    public FlightService(DataStore db) { this.db = db; }

    public List<Flight> search(String from, String to, String date) {
        List<Flight> out = new ArrayList<>();
        for (Flight f : db.flights.values()) {
            if (from != null && !from.isBlank() && !f.from.equalsIgnoreCase(from.trim())) continue;
            if (to != null && !to.isBlank() && !f.to.equalsIgnoreCase(to.trim())) continue;
            if (date != null && !date.isBlank() && !f.date.equals(date.trim())) continue;
            out.add(f);
        }
        out.sort(Comparator.comparing((Flight f) -> f.date).thenComparing(f -> f.departure));
        return out;
    }

    public Flight get(String id) { return db.flights.get(id); }

    public Flight create(Map<String, Object> m) {
        Flight f = Flight.fromJson(m);
        if (f.id == null || f.id.isBlank()) f.id = "f-" + UUID.randomUUID().toString().substring(0, 8);
        synchronized (db) { db.flights.put(f.id, f); db.saveQuietly(); }
        return f;
    }

    public Flight update(String id, Map<String, Object> m) {
        synchronized (db) {
            Flight f = db.flights.get(id);
            if (f == null) return null;
            Set<String> keepSeats = f.bookedSeats;
            Flight n = Flight.fromJson(m);
            n.id = id; n.bookedSeats = keepSeats;
            db.flights.put(id, n); db.saveQuietly();
            return n;
        }
    }

    public boolean delete(String id) {
        synchronized (db) {
            // refuse delete if active bookings exist
            for (Booking b : db.bookings.values()) {
                if (b.flightId.equals(id) && b.status.equals("CONFIRMED")) return false;
            }
            db.saveQuietly();
            return db.flights.remove(id) != null;
        }
    }

    public static String now() { return LocalDateTime.now().toString(); }
}
