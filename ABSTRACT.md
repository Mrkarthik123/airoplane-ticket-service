# ✈ Airoplane Ticket Service — Project Abstract

**Project type:** Full-stack flight-ticket reservation system (backend + native desktop app + web/PWA + Android)
**Course category:** Software Engineering / Full-Stack Development mini-project
**Language of the codebase:** Java (primary), HTML/CSS/JavaScript (frontend), Shell (build & packaging)

---

## 1. Abstract

The **Airoplane Ticket Service** is a complete, self-contained online flight-ticket booking system developed
in **pure Java with zero external runtime dependencies**, together with a **native Java Swing desktop
application**, a **responsive web / installable PWA frontend**, and an **Android (Capacitor) wrapper**.

The system automates the airline reservation workflow end to end. A customer registers or logs in, searches
flights by source airport, destination airport and travel date, selects a seat from a live 30×6 seat map,
enters passenger details (name, age, gender), pays through a simulated payment gateway, and receives a
printable e-ticket carrying a unique six-character PNR. A critical design decision in this project is the
**admin approval gate**: every new booking is created in the `PENDING` state, so the seat is reserved but
**no e-ticket is issued** until a verified administrator reviews and confirms the booking from the admin
dashboard. Only after confirmation does the ticket become valid, printable, and counted towards revenue.
This mirrors the real airline workflow of ticket issuance by a ticketing officer and demonstrates a clean
separation of customer-facing and administrator responsibilities.

Administrators get a separate dashboard with live statistics (flights, users, total bookings, active,
pending and revenue), full CRUD management of flights, and a pending-approval queue in which a selected
booking can be confirmed or cancelled with one click. A dedicated airports module visualises the Indian
domestic network on a custom-painted map with animated markers and route lines.

The backend is built on the JDK's own `com.sun.net.httpserver.HttpServer`, with a hand-written JSON
parser/serializer, SHA-256 password hashing, token-based session management, file-backed JSON persistence
and a layered architecture (`model → service → store → ui/util`). The same service layer is reused by
three front-ends, so the desktop app, the browser UI and the phone app all run identical business rules.
The macOS build is produced with `jpackage` into a signed `.app` bundle, and the whole project is packaged
as a portable JAR, a double-click `.command` launcher and an Android APK — all from the same source tree.

**Keywords:** Java, Swing, HTTP server, JSON, authentication, SHA-256, seat booking, admin approval
workflow, PWA, Capacitor, jpackage, data structures, file-based persistence.

---

## 2. Languages & technologies used

| Language / technology | Where it is used | Purpose |
|---|---|---|
| **Java 17** (SE, no frameworks) | `src/main/java/com/airline/**` — 24 files, ~3,093 LOC | Backend API, business logic, persistence, **and the entire native desktop GUI (Swing)** |
| **HTML5 / CSS3** | `src/main/resources/static/index.html` (199 lines), `styles.css` (85 lines) | Responsive web / PWA layout, glass-morphism UI, seat-map grid, modals |
| **JavaScript (ES6, vanilla)** | `src/main/resources/static/app.js` (364 lines) | Fetch-based API client, search, booking flow, admin dashboard, service worker |
| **JSON** | `data/flights.json`, `users.json`, `bookings.json` | Data persistence format + every API request/response body |
| **Java Swing / AWT** | `com.airline.ui.*` (13 classes) | Native macOS desktop application: frame, tabs, dialogs, custom painting, printing |
| **Service Worker / Web App Manifest** | `static/sw.js`, `manifest.webmanifest` | PWA installability and offline caching for phones |
| **Node.js + Capacitor 7** | `mobile/` | Wraps the same web UI as a native Android app (`SkyJet.apk`) |
| **Gradle / Android SDK** | `mobile/android/` | Android build system for the APK |
| **Shell (Bash)** | `run.sh`, `AiroplaneTicketService.command`, `mobile/build-apk.sh` | One-command compile & launch, double-click launcher, APK build |
| **Maven** | `pom.xml` | Standard project descriptor (`mvn package` → runnable JAR) |
| **JDK `jpackage` + `codesign`** | Build pipeline | Produces the signed `AiroplaneTicketService.app` macOS bundle |
| **XML / JSON config** | `pom.xml`, `capacitor.config.js`, `manifest.webmanifest` | Build + app metadata |

> **Design constraint:** the Java side deliberately uses **no third-party libraries** — the HTTP server,
> JSON parser, password hashing, session store and GUI toolkit all come from the JDK. This keeps the
> application auditable, easy to build offline, and runnable on any machine with Java 17 installed.

---

## 3. System architecture

```
┌───────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                            │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │ Native desktop   │  │  Web / PWA       │  │  Android app       │  │
│  │ (Java Swing)     │  │  (HTML/CSS/JS)   │  │  (Capacitor)       │  │
│  │ MainFrame,       │  │  index.html,     │  │  wraps the same    │  │
│  │ LoginDialog,     │  │  app.js,         │  │  web UI + server   │  │
│  │ BookingDialog…   │  │  styles.css      │  │  URL for phone     │  │
│  └────────┬─────────┘  └────────┬─────────┘  └──────────┬─────────┘  │
└───────────┼─────────────────────┼──────────────────────┼────────────┘
            │  in-process calls    │  HTTP + JSON         │  HTTP + JSON
            │  (no serialization)  │  /api/**             │
┌───────────▼─────────────────────▼──────────────────────▼────────────┐
│                     SERVICE LAYER (business rules)                   │
│  AuthService        FlightService        BookingService             │
│  register/login     search/create        create → PENDING            │
│  SHA-256 + token    update/delete        confirm (admin) → CONFIRMED│
│  session lookup     seed + availability cancel → CANCELLED          │
└───────────┬──────────────────────────────────────────────────────────┘
            │
┌───────────▼─────────────┐        ┌─────────────────────────────────┐
│      MODEL LAYER        │        │      STORE LAYER (DataStore)    │
│  Flight, User, Booking  │◄──────►│  flights.json  users.json       │
│  (POJOs + toJson)      │        │  bookings.json  sessions (mem)  │
└─────────────────────────┘        └─────────────────────────────────┘
```

**Layered design:** the UI never touches files directly; the services never format HTTP; the store is the
only component that knows about the file system. The same `BookingService` therefore enforces the
approval rule identically in the desktop app, the browser and on the phone.

---

## 4. Main code

> The listings below are taken **verbatim from the source tree**; the only edits are a few inline
> explanatory comments and alignment spacing.

### 4.1 Booking creation → `PENDING` (core business rule)

`src/main/java/com/airline/service/BookingService.java`

```java
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
        b.totalPrice = total;
        b.status = "PENDING";          // ← admin must confirm before the ticket is issued
        b.paymentId = paymentId == null ? "" : paymentId;
        b.createdAt = LocalDateTime.now().toString();
        f.bookedSeats.addAll(seats);   // seat is held while the booking waits
        db.bookings.put(b.pnr, b);
        db.saveQuietly();
        r.booking = b;
    }
    return r;
}
```

### 4.2 Admin confirmation (the approval gate)

`src/main/java/com/airline/service/BookingService.java`

```java
/** Admin approves a pending booking and issues the ticket. */
public Result confirm(String pnr, boolean isAdmin) {
    Result r = new Result();
    synchronized (db) {
        Booking b = db.bookings.get(pnr);
        if (b == null) { r.error = "Booking not found."; return r; }
        if (!isAdmin) { r.error = "Only an admin can confirm a booking."; return r; }
        if (b.status.equals("CONFIRMED")) { r.error = "Booking " + pnr + " is already confirmed."; return r; }
        if (b.status.equals("CANCELLED")) { r.error = "Booking " + pnr + " was cancelled."; return r; }
        b.status = "CONFIRMED";
        b.confirmedAt = LocalDateTime.now().toString();
        db.saveQuietly();
        r.booking = b;
    }
    return r;
}
```

### 4.3 HTTP server and API routing

`src/main/java/com/airline/App.java`

```java
server.createContext("/api/", app::handleApi);
server.createContext("/", app::handleStatic);
server.setExecutor(Executors.newFixedThreadPool(8));
server.start();

// POST /api/bookings  → create a booking (status = PENDING)
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
    BookingService.Result r = bookings.create(u.id, flightId, pax, seats,
            Json.str(b, "paymentId", ""), Json.dbl(b, "paidAmount", 0));
    if (r.error != null) { json(ex, 400, err(r.error)); return; }
    json(ex, 201, ok(Map.of("booking", enrich(r.booking)))); return;
}

// POST /api/bookings/{pnr}/confirm  → admin only
if (path.startsWith("/api/bookings/") && path.endsWith("/confirm") && method.equals("POST")) {
    User u = requireAdmin(ex); if (u == null) return;
    String pnr = path.substring("/api/bookings/".length(), path.length() - "/confirm".length());
    BookingService.Result r = bookings.confirm(pnr, true);
    if (r.error != null) { json(ex, 400, err(r.error)); return; }
    json(ex, 200, ok(Map.of("booking", enrich(r.booking)))); return;
}
```

### 4.4 Authentication with hashed passwords and token sessions

`src/main/java/com/airline/service/AuthService.java` + `util/Passwords.java`

```java
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
    db.sessions.put(t, u.id);      // token → userId, stored in memory
    return t;
}

// SHA-256 hashing (no plaintext password is ever written to disk)
public static String hash(String plain) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] d = md.digest(plain.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte b : d) sb.append(String.format("%02x", b));
    return sb.toString();
}
```

### 4.5 File-backed persistence with automatic seeding

`src/main/java/com/airline/store/DataStore.java`

```java
public synchronized void load() throws IOException {
    Files.createDirectories(dataDir);
    loadFlights(); loadUsers(); loadBookings();
    if (flights.isEmpty()) seedFlights();     // 10 seeded Indian domestic flights
    if (users.isEmpty()) seedUsers();         // admin@skyjet.in + demo@skyjet.in
    rollFlightDatesForward();                 // keep demo dates always in the future
    saveAll();
}
```

### 4.6 Native desktop UI — entry point and window

`src/main/java/com/airline/ui/DesktopLauncher.java` and `MainFrame.java`

```java
/** Starts the native desktop UI: login → main window → (logout) → login … */
public static void launch(DataStore db, AuthService auth, FlightService flights,
                           BookingService bookings, String serverUrl) {
    SwingUtilities.invokeLater(() -> {
        LoginDialog login = new LoginDialog(null, auth);
        login.setVisible(true);
        User user = login.getUser();
        if (user == null) { System.exit(0); return; }
        MainFrame frame = new MainFrame(db, auth, flights, bookings, user, login.getToken(), serverUrl);
        frame.setOnLogout(() -> SwingUtilities.invokeLater(
                () -> launch(db, auth, flights, bookings, serverUrl)));
        frame.setVisible(true);
    });
}
```

```java
// MainFrame — tabbed native window
tabs.addTab("🔍 Search",     searchPanel);
tabs.addTab("🗺 Airports",   airportsPanel);
tabs.addTab("🎫 My bookings", bookingsPanel);
if (user.role.equals("admin")) {
    adminPanel = new AdminPanel(db, flights, bookings, this::refreshAll);
    tabs.addTab("⚙ Admin", adminPanel);
}
```

### 4.7 Interactive seat map (30 × 6) with live availability

`src/main/java/com/airline/ui/BookingDialog.java`

```java
private void renderSeats() {
    seatPanel.removeAll();
    for (int row = 1; row <= 30; row++) {                       // 30 rows
        for (String col : new String[]{"A", "B", "C", "", "D", "E", "F"}) {   // 6 seats + aisle
            if (col.isEmpty()) {                                 // aisle: row number label
                JLabel l = new JLabel(String.valueOf(row), SwingConstants.CENTER);
                l.setFont(Theme.font(10));
                l.setForeground(Theme.MUTED);
                seatPanel.add(l);
                continue;
            }
            String seat = row + col;
            boolean taken = flight.bookedSeats.contains(seat);
            JButton b = new JButton(seat);
            b.setFont(Theme.font(10));
            b.setBorder(BorderFactory.createLineBorder(taken ? new Color(0xCBD5E1) : Theme.LINE));
            b.setBackground(taken ? new Color(0xE2E8F0) : Color.WHITE);
            b.setForeground(taken ? new Color(0x94A3B8) : Theme.INK);
            b.setEnabled(!taken);
            b.addActionListener(e -> {
                if (selectedSeats.contains(seat)) {
                    selectedSeats.remove(seat);
                    b.setBackground(Color.WHITE);
                    b.setForeground(Theme.INK);
                } else {
                    if (selectedSeats.size() >= travellers) {
                        JOptionPane.showMessageDialog(this, "You can pick " + travellers + " seat(s) only.");
                        return;
                    }
                    selectedSeats.add(seat);
                    b.setBackground(Theme.PRIMARY);
                    b.setForeground(Color.WHITE);
                }
                updateTotal();
            });
            seatPanel.add(b);
        }
    }
    seatPanel.revalidate();
    seatPanel.repaint();
}
```

### 4.8 Admin approval action in the dashboard

`src/main/java/com/airline/ui/AdminPanel.java`

```java
/** Approves the selected pending booking — the e-ticket is issued only after this. */
private void confirmSelectedBooking() {
    int r = bookingTable.getSelectedRow();
    if (r < 0) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
    String pnr = (String) bookingTable.getValueAt(r, 0);
    Booking b = db.bookings.get(pnr);
    if (!"PENDING".equals(b.status)) {
        JOptionPane.showMessageDialog(this,
            "Booking " + pnr + " is " + b.status + " — only pending bookings can be confirmed.");
        return;
    }
    BookingService.Result res = bookings.confirm(pnr, true);
    if (res.error != null) JOptionPane.showMessageDialog(this, res.error);
    else JOptionPane.showMessageDialog(this, "Booking " + pnr + " confirmed. E-ticket issued.");
    refresh();
    onChanged.changed();
}
```

### 4.9 Frontend API client (vanilla JavaScript)

`src/main/resources/static/app.js`

```javascript
function headers(json = true) {
  const h = {};
  if (json) h['Content-Type'] = 'application/json';
  if (state.token) h['Authorization'] = 'Bearer ' + state.token;
  return h;
}

async function api(path, opts = {}) {
  const r = await fetch(API + path, opts);
  const j = await r.json().catch(() => ({ ok: false, error: 'Bad response' }));
  if (!r.ok || !j.ok) throw new Error(j.error || ('HTTP ' + r.status));
  return j;
}

// customer submits the booking → it is created as PENDING
const j = await api('/api/bookings', {
  method: 'POST', headers: headers(),
  body: JSON.stringify({ flightId: state.bookingFlight.id, passengers: pax, seats: state.seats,
                         paymentId: pay.payment.paymentId, paidAmount: amount })
});
toast('Booking submitted — PNR ' + j.booking.pnr + ' is PENDING until an admin confirms it.');

// admin approves it
window.adminConfirmBooking = async (pnr) => {
  if (!confirm('Confirm booking ' + pnr + ' and issue the e-ticket?')) return;
  try {
    await api('/api/bookings/' + pnr + '/confirm', { method: 'POST', headers: headers() });
    toast('Booking ' + pnr + ' confirmed — e-ticket issued.');
    loadAdminBookings(); loadAdmin();
  } catch (err) { toast(err.message); }
};
```

---

## 5. Feature summary

| # | Module | Description |
|---|---|---|
| 1 | **Authentication** | Register / login, SHA-256 hashed passwords, UUID bearer tokens, in-memory sessions, `customer` / `admin` roles |
| 2 | **Flight search** | Source, destination, date; availability, price and duration; seeded Indian domestic routes (DEL, BOM, BLR, HYD, MAA) |
| 3 | **Seat selection** | Interactive 30×6 seat map (180 seats), aisle column, taken/selected states, per-booking seat holds, concurrency-safe re-check |
| 4 | **Passenger details** | Name, age and gender per traveller, with column headers, placeholder hints and age validation (numeric, 1–120) |
| 5 | **Mock payment** | Test-card validation (12–19 digits), simulated `PAY-XXXXXXXX` transaction, mock refund on cancellation |
| 6 | **Admin approval workflow** | `PENDING → CONFIRMED / CANCELLED`; seat held while pending; ticket issued only after admin confirmation; `confirmedAt` audit stamp |
| 7 | **E-ticket** | PNR, flight details, passenger table, seat numbers, total; printable through the Java printing API and the browser print dialog |
| 8 | **My bookings** | History with status badges, pending notice, refresh-to-check-approval, cancellation with seat release |
| 9 | **Admin dashboard** | Live stats (flights, users, bookings, active, pending, revenue), flight CRUD, all-bookings table with confirm/cancel |
| 10 | **Airports module** | Custom-painted map of India with animated markers and route network, airport list, "fly from here" shortcut |
| 11 | **Native desktop app** | Java Swing window (13 classes) packaged as a signed macOS `.app` via `jpackage` — no browser required |
| 12 | **Web / PWA** | Responsive glass-morphism UI, service worker, installable on phones, same backend over LAN |
| 13 | **Android app** | Capacitor wrapper of the same UI, shipped as `SkyJet.apk` |
| 14 | **Persistence** | `data/*.json` written atomically; thread-safe via `synchronized` blocks and `ConcurrentHashMap` |

---

## 6. API reference (selected)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | – | Create a customer account, returns a token |
| POST | `/api/auth/login` | – | Returns `{ user, token }` |
| GET | `/api/auth/me` | Bearer | Current user profile |
| GET | `/api/flights?from=&to=&date=` | – | Flight search |
| POST/PUT/DELETE | `/api/flights…` | Admin | Flight management |
| POST | `/api/payments/mock` | Bearer | Simulated payment |
| POST | `/api/bookings` | Bearer | Create booking → status `PENDING` |
| GET | `/api/bookings` | Bearer | My bookings (all bookings if admin) |
| POST | `/api/bookings/{pnr}/confirm` | **Admin** | Approve a pending booking, issue the e-ticket |
| POST | `/api/bookings/{pnr}/cancel` | Owner/Admin | Cancel and release seats |
| GET | `/api/admin/stats` | Admin | Flights / users / bookings / active / pending / revenue |

---

## 7. How to run

```bash
# 1) any machine with JDK 17 — compile and run (no Maven needed)
./run.sh
#    → desktop window + http://localhost:8080

# 2) Maven
mvn clean package && java -jar target/airplane-ticket-service-1.0.0.jar

# 3) macOS native app
open AiroplaneTicketService.app          # or: open "/Applications/Airoplane Ticket Service.app"
#    (rebuild with: jpackage --name AiroplaneTicketService --input /tmp/pkg --main-jar AiroplaneTicketService.jar
#                   --type app-image --dest . && codesign --force --deep --sign - AiroplaneTicketService.app)

# 4) Android APK
cd mobile && npm run build-apk            # → mobile/android/app/build/outputs/apk/…/app-debug.apk
```

**Demo accounts**

| Role | Email | Password |
|---|---|---|
| Customer | `demo@skyjet.in` | `demo123` |
| Admin | `admin@skyjet.in` | `admin123` |

---

## 8. Testing performed

| Test | Result |
|---|---|
| 10-step service test: create → `PENDING`, customer confirm rejected, admin confirm → `CONFIRMED`, double confirm rejected, cancel-while-pending releases the seat | **Pass** |
| Live HTTP test: `201 PENDING` → `403` for a customer → admin `200 CONFIRMED` with `confirmedAt` → `pendingBookings: 1` | **Pass** |
| UI component test: pending badge, amber status pill, disabled print button, admin "✓ Confirm booking" action | **Pass** |
| Pixel-render test of the pending card (amber badge 3,424 px, border 288 px) and of the login screen's input outlines | **Pass** |
| Clean `javac` compile of all 24 Java files | **Pass** |
| macOS bundle launch check (`lsappinfo` foreground app + HTTP 200) | **Pass** |

---

## 9. Limitations & future scope

**Limitations** — SHA-256 without salting (bcrypt/Argon2 would be stronger for production), single-process
file storage (no concurrent multi-user server), mocked payment gateway, and map tiles drawn offline rather
than streamed from a live tile server.

**Future scope** — real payment gateway integration (Razorpay/Stripe), seat-hold expiry timers, e-mail and
SMS ticket delivery with a QR code, PDF e-ticket generation, multi-airline support with fare classes and
dynamic pricing, WebSocket push notifications so a customer sees the approval instantly, and migration of
the JSON store to SQLite or PostgreSQL.

---

## 10. Conclusion

The project demonstrates that a complete, production-shaped booking system can be written with **no
third-party backend dependencies** while still delivering a polished native desktop experience, a modern
web interface and a mobile app from a single codebase. Its strongest feature — the **admin approval gate**
— enforces a real airline business rule end to end: a customer pays and reserves a seat, but the e-ticket
is issued only after an authorised administrator approves the booking, and every transition is validated,
persisted and surfaced in the UI. The layered architecture makes the same business rules reusable across
Java Swing, HTML/JavaScript and Android, which is the central engineering lesson of the project.

---

*Repository: `https://github.com/Mrkarthik123/airoplane-ticket-service` · Branch: `main`*

*Spoken version of this abstract: see **[PRESENTATION.md](PRESENTATION.md)** — a 6-minute read-aloud script,
a 60-second short version, and prepared Q&A.*
