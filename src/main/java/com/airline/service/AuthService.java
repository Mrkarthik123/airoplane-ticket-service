package com.airline.service;

import com.airline.model.User;
import com.airline.store.DataStore;
import com.airline.util.Passwords;
import java.util.UUID;

/** Register / login / session lookup. */
public class AuthService {
    private final DataStore db;
    public AuthService(DataStore db) { this.db = db; }

    public static class AuthResult {
        public User user; public String token; public String error;
    }

    public AuthResult register(String name, String email, String password) {
        AuthResult r = new AuthResult();
        if (name == null || name.isBlank() || email == null || !email.contains("@") || password == null || password.length() < 4) {
            r.error = "Name, valid email and 4+ char password required."; return r;
        }
        synchronized (db) {
            if (db.findUserByEmail(email) != null) { r.error = "Email already registered."; return r; }
            User u = new User();
            u.id = "u-" + UUID.randomUUID().toString().substring(0, 8);
            u.name = name.trim(); u.email = email.toLowerCase().trim();
            u.passwordHash = Passwords.hash(password); u.role = "customer";
            db.users.put(u.id, u);
            db.saveQuietly();
            r.user = u; r.token = issueToken(u);
        }
        return r;
    }

    public AuthResult login(String email, String password) {
        AuthResult r = new AuthResult();
        User u = db.findUserByEmail(email == null ? "" : email.trim());
        if (u == null || !Passwords.verify(password == null ? "" : password, u.passwordHash)) {
            r.error = "Invalid email or password. Try demo@skyjet.in / demo123"; return r;
        }
        r.user = u; r.token = issueToken(u);
        return r;
    }

    private String issueToken(User u) {
        String t = UUID.randomUUID().toString();
        db.sessions.put(t, u.id);
        return t;
    }

    public User me(String token) {
        if (token == null || token.isEmpty()) return null;
        String uid = db.sessions.get(token);
        return uid == null ? null : db.users.get(uid);
    }

    public void logout(String token) { if (token != null) db.sessions.remove(token); }
}
