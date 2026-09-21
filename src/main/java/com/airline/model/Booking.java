package com.airline.model;

import java.util.*;

/** Booking / e-ticket entity. PNR is the public ticket id. */
public class Booking {
    public String pnr;
    public String userId;
    public String flightId;
    public List<Map<String, Object>> passengers = new ArrayList<>();
    public List<String> seats = new ArrayList<>();
    public double totalPrice;
    public String status = "CONFIRMED"; // CONFIRMED | CANCELLED
    public String paymentId;
    public String createdAt;

    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pnr", pnr); m.put("userId", userId); m.put("flightId", flightId);
        m.put("passengers", passengers); m.put("seats", seats);
        m.put("totalPrice", totalPrice); m.put("status", status);
        m.put("paymentId", paymentId); m.put("createdAt", createdAt);
        return m;
    }

    @SuppressWarnings("unchecked")
    public static Booking fromJson(Map<String, Object> m) {
        Booking b = new Booking();
        b.pnr = String.valueOf(m.getOrDefault("pnr", ""));
        b.userId = String.valueOf(m.getOrDefault("userId", ""));
        b.flightId = String.valueOf(m.getOrDefault("flightId", ""));
        Object p = m.get("passengers");
        if (p instanceof List) for (Object o : (List<Object>) p) {
            if (o instanceof Map) b.passengers.add((Map<String, Object>) o);
        }
        Object s = m.get("seats");
        if (s instanceof List) for (Object o : (List<Object>) s) b.seats.add(String.valueOf(o));
        Object t = m.get("totalPrice");
        b.totalPrice = t instanceof Number ? ((Number) t).doubleValue() : 0;
        b.status = String.valueOf(m.getOrDefault("status", "CONFIRMED"));
        b.paymentId = String.valueOf(m.getOrDefault("paymentId", ""));
        b.createdAt = String.valueOf(m.getOrDefault("createdAt", ""));
        return b;
    }
}
