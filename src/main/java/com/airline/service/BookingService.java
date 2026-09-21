package com.airline.service;

import com.airline.model.*;
import com.airline.store.DataStore;
import java.time.LocalDateTime;
import java.util.*;

/** Booking creation / cancellation / queries + mock payments. */
public class BookingService {
    private final DataStore db;
    public BookingService(DataStore db) { this.db = db; }

    public static class Result {
        public Booking booking; public String error;
    }

    /** Mock payment: accepts any 12-19 digit card, always approves. */
    public Map<String, Object> mockPay(double amount, String cardLast4) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("paymentId", "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        m.put("amount", amount);
        m.put("status", "SUCCESS");
        m.put("cardLast4", cardLast4);
        m.put("at", LocalDateTime.now().toString());
        return m;
    }

    public Result create(String userId, String flightId, List<Map<String, Object>> passengers,
                         List<String> seats, String paymentId, double paidAmount) {
        Result r = new Result();
        synchronized (db) {
            Flight f = db.flights.get(flightId);
            if (f == null) { r.error = "Flight not found."; return r; }
            if (passengers == null || passengers.isEmpty()) { r.error = "Add at least one passenger."; return r; }
            if (seats == null || seats.size() != passengers.size()) { r.error = "Seats must match passengers."; return r; }
            for (String s : seats) {
                if (f.bookedSeats.contains(s)) { r.error = "Seat " + s + " just got booked. Pick another."; return r; }
            }
            double total = f.price * passengers.size();
            Booking b = new Booking();
            b.pnr = genPnr();
            b.userId = userId; b.flightId = flightId;
            b.passengers = passengers; b.seats = new ArrayList<>(seats);
            b.totalPrice = total; b.status = "CONFIRMED";
            b.paymentId = paymentId == null ? "" : paymentId;
            b.createdAt = LocalDateTime.now().toString();
            f.bookedSeats.addAll(seats);
            db.bookings.put(b.pnr, b);
            db.saveQuietly();
            r.booking = b;
        }
        return r;
    }

    public Result cancel(String pnr, String requesterId, boolean isAdmin) {
        Result r = new Result();
        synchronized (db) {
            Booking b = db.bookings.get(pnr);
            if (b == null) { r.error = "Booking not found."; return r; }
            if (!isAdmin && !b.userId.equals(requesterId)) { r.error = "Not your booking."; return r; }
            if (b.status.equals("CANCELLED")) { r.error = "Already cancelled."; return r; }
            b.status = "CANCELLED";
            Flight f = db.flights.get(b.flightId);
            if (f != null) f.bookedSeats.removeAll(b.seats);
            db.saveQuietly();
            r.booking = b;
        }
        return r;
    }

    public List<Booking> forUser(String userId) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : db.bookings.values()) if (b.userId.equals(userId)) out.add(b);
        out.sort(Comparator.comparing((Booking b) -> b.createdAt).reversed());
        return out;
    }

    public List<Booking> all() {
        List<Booking> out = new ArrayList<>(db.bookings.values());
        out.sort(Comparator.comparing((Booking b) -> b.createdAt).reversed());
        return out;
    }

    private String genPnr() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random r = new Random();
        String pnr;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
            pnr = sb.toString();
        } while (db.bookings.containsKey(pnr));
        return pnr;
    }
}
