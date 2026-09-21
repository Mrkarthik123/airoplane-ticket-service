package com.airline.model;

import java.util.*;

/** Flight entity. */
public class Flight {
    public String id;
    public String airline;
    public String flightNumber;
    public String from;
    public String to;
    public String date;        // yyyy-MM-dd
    public String departure;   // HH:mm
    public String arrival;     // HH:mm
    public int durationMin;
    public double price;       // INR per adult
    public int totalSeats;
    public Set<String> bookedSeats = new LinkedHashSet<>();

    public Flight() {}

    public Flight(String id, String airline, String flightNumber, String from, String to,
                  String date, String departure, String arrival, int durationMin,
                  double price, int totalSeats) {
        this.id = id; this.airline = airline; this.flightNumber = flightNumber;
        this.from = from; this.to = to; this.date = date;
        this.departure = departure; this.arrival = arrival;
        this.durationMin = durationMin; this.price = price; this.totalSeats = totalSeats;
    }

    public int availableSeats() { return Math.max(0, totalSeats - bookedSeats.size()); }

    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("airline", airline); m.put("flightNumber", flightNumber);
        m.put("from", from); m.put("to", to); m.put("date", date);
        m.put("departure", departure); m.put("arrival", arrival);
        m.put("durationMin", durationMin); m.put("price", price);
        m.put("totalSeats", totalSeats); m.put("availableSeats", availableSeats());
        m.put("bookedSeats", new ArrayList<>(bookedSeats));
        return m;
    }

    @SuppressWarnings("unchecked")
    public static Flight fromJson(Map<String, Object> m) {
        Flight f = new Flight();
        f.id = String.valueOf(m.getOrDefault("id", UUID.randomUUID().toString()));
        f.airline = String.valueOf(m.getOrDefault("airline", ""));
        f.flightNumber = String.valueOf(m.getOrDefault("flightNumber", ""));
        f.from = String.valueOf(m.getOrDefault("from", "")).toUpperCase();
        f.to = String.valueOf(m.getOrDefault("to", "")).toUpperCase();
        f.date = String.valueOf(m.getOrDefault("date", ""));
        f.departure = String.valueOf(m.getOrDefault("departure", ""));
        f.arrival = String.valueOf(m.getOrDefault("arrival", ""));
        Object d = m.get("durationMin");
        f.durationMin = d instanceof Number ? ((Number) d).intValue() : 0;
        Object p = m.get("price");
        f.price = p instanceof Number ? ((Number) p).doubleValue() : 0;
        Object t = m.get("totalSeats");
        f.totalSeats = t instanceof Number ? ((Number) t).intValue() : 180;
        Object b = m.get("bookedSeats");
        if (b instanceof List) for (Object s : (List<Object>) b) f.bookedSeats.add(String.valueOf(s));
        return f;
    }
}
