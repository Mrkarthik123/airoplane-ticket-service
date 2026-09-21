package com.airline.model;

import java.util.*;

/** User entity (role = admin | customer). */
public class User {
    public String id;
    public String name;
    public String email;
    public String passwordHash;
    public String role = "customer";

    public Map<String, Object> toJson(boolean includeHash) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("name", name); m.put("email", email); m.put("role", role);
        if (includeHash) m.put("passwordHash", passwordHash);
        return m;
    }

    public Map<String, Object> publicJson() { return toJson(false); }

    @SuppressWarnings("unchecked")
    public static User fromJson(Map<String, Object> m) {
        User u = new User();
        u.id = String.valueOf(m.getOrDefault("id", UUID.randomUUID().toString()));
        u.name = String.valueOf(m.getOrDefault("name", ""));
        u.email = String.valueOf(m.getOrDefault("email", "")).toLowerCase().trim();
        u.passwordHash = String.valueOf(m.getOrDefault("passwordHash", ""));
        u.role = String.valueOf(m.getOrDefault("role", "customer"));
        return u;
    }
}
