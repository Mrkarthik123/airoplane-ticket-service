# ✈ Airoplane Ticket Service (Java)

> 📄 **[ABSTRACT.md](ABSTRACT.md)** — project abstract, languages/technologies used, architecture diagram,
> main code listings, feature summary, API reference, testing and future scope (for project display/submission).
>
> 🎤 **[PRESENTATION.md](PRESENTATION.md)** — the same project written as a spoken script you can read
> straight from the screen: 6-minute presentation, a 60-second short version, and likely Q&A with answers.

Full flight-ticket booking app in **pure Java (no external dependencies)** + HTML/CSS/JS frontend.

- **Search & booking flow** — from/to/date, flight list, seat map (30×6), passengers, mock payment
- **Admin approval flow** — a new booking is **PENDING**; the e-ticket is issued only after an admin confirms it
- **Auth + My bookings** — register/login (token), booking history, view/print ticket, cancel with refund
- **Payments mock** — test card validation, fake `PAY-XXXX` transactions
- **Admin dashboard** — revenue/booking stats, pending-approval queue, add/edit/delete flights, all-bookings tab with search
- **Airport map** — real interactive map (Leaflet + OpenStreetMap) with airport locations and route network
- **Persistence** — `data/*.json` files (flights, users, bookings)

## ⏳ Booking approval flow

1. The customer books a flight and pays (mock) → the booking is created with status **PENDING**.
   The seat is held so nobody else can take it, but **no e-ticket is issued yet**.
2. The customer sees the booking as `PENDING · awaiting admin` with the note
   *"Waiting for admin confirmation"* — printing is locked.
3. The admin opens **Admin → 🎫 Bookings**, selects the pending row and clicks **✓ Confirm booking**.
4. The booking becomes **CONFIRMED**, the e-ticket is issued and printing unlocks for the customer.
   A pending booking can also be cancelled (which releases the seat).

Status values: `PENDING` → `CONFIRMED` | `CANCELLED`. Revenue counts confirmed bookings only.

## 🖥️ Desktop app (macOS) — native Java window

The app is a **native Java/Swing desktop application** (no browser needed):

- **`AiroplaneTicketService.app`** — double-clickable app (bundled Java runtime, custom plane icon).
  Opens a native window: login → **Search · Airports · My bookings · Admin** tabs, animated sky-and-plane
  background, 30×6 seat map, passenger forms, mock payment, printable e-ticket.
  Already installed in `/Applications` as **Airoplane Ticket Service**.
- **`AiroplaneTicketService.command`** — double-click launcher for the same native window (needs Java 17+).
- **`AiroplaneTicketService.jar`** — portable JAR (any OS with Java 17+):
  ```bash
  java -jar AiroplaneTicketService.jar --ui
  ```
- The HTTP server keeps running in the background on port 8080, so phones on the same Wi-Fi can still
  reach the app at `http://<laptop-ip>:8080`.
- Stop the server: `lsof -ti tcp:8080 | xargs kill` (or close the app window).
- The app stores its data in `AiroplaneTicketService.app/Contents/app/data/`.
- Copying the `.app` to another Mac may require a right-click → **Open** (unsigned local build).

To rebuild the app files after code changes:
```bash
./run.sh                       # compiles to out/ and starts the server
jar --create --file AiroplaneTicketService.jar --main-class com.airline.App -C out .
jpackage --name AiroplaneTicketService --input <dir-with-jar> \
  --main-jar AiroplaneTicketService.jar --type app-image --dest . --app-version 1.0.0
```

## 📱 Mobile app (Android APK + PWA)

- **`SkyJet.apk`** — native Android app. Copy to your phone, install, enter
  the server URL (`http://<laptop-ip>:8080`) on first launch. Same Wi-Fi as the laptop.
- **PWA** — on your phone browser open `http://<laptop-ip>:8080` → Add to Home Screen.
  Works on Android + iPhone, runs fullscreen, shell works offline.
- Details + rebuild steps: [`mobile/README.md`](mobile/README.md).

## Run from source (no Maven needed — just Java 17+)

```bash
cd airplane-ticket-service
chmod +x run.sh
./run.sh
# open http://localhost:8080
```

Custom port: `PORT=9090 ./run.sh`

## Run with Maven (optional)

```bash
mvn package
java -cp target/classes com.airline.App --port 8080
```

## Demo accounts

| Role | Email | Password |
|------|-------|----------|
| Customer | `demo@skyjet.in` | `demo123` |
| Admin | `admin@skyjet.in` | `admin123` |

## API

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | — | `{name,email,password}` |
| POST | `/api/auth/login` | — | `{email,password}` → `{user,token}` |
| GET | `/api/auth/me` | Bearer | current user |
| GET | `/api/flights?from=DEL&to=BOM&date=2026-09-19` | — | search |
| GET | `/api/flights/{id}` | — | one flight + seat map |
| POST/PUT/DELETE | `/api/flights…` | Admin | manage flights |
| POST | `/api/payments/mock` | — | `{amount,cardNumber}` → fake success |
| POST | `/api/bookings` | Bearer | `{flightId,passengers[],seats[],paymentId}` |
| GET | `/api/bookings` | Bearer | my bookings (all if admin) |
| GET | `/api/bookings/{pnr}` | Bearer | e-ticket |
| POST | `/api/bookings/{pnr}/cancel` | Bearer | cancel + free seats |
| POST | `/api/bookings/{pnr}/confirm` | Admin | approve a pending booking, issue the e-ticket |
| GET | `/api/admin/stats` | Admin | flights/users/revenue/pending |

## Project layout

```
src/main/java/com/airline/
  App.java                 ← HTTP server + routing (port 8080)
  model/Flight.java, User.java, Booking.java
  store/DataStore.java     ← JSON file DB + seed data
  service/AuthService.java, FlightService.java, BookingService.java
  util/Json.java, HttpUtil.java, Passwords.java
  ui/                      ← native Swing desktop app
    MainFrame.java         ← header + Search/Airports/Bookings/Admin tabs
    LoginDialog.java       ← login + register
    SearchPanel.java       ← search form + flight cards
    AirportsPanel.java     ← painted map + airport list
    BookingDialog.java     ← seat map, passengers, mock payment
    TicketDialog.java      ← e-ticket with print
    BookingsPanel.java     ← my bookings list
    AdminPanel.java        ← stats, flight CRUD, all bookings
    SkyPanel.java          ← animated plane background
    Theme.java             ← colors, fonts, widget factories
src/main/resources/static/ ← index.html, styles.css, app.js (web/PWA UI for phones)
data/                      ← auto-created flights.json, users.json, bookings.json
```

## Notes

- Passwords are SHA-256 hashed (demo-grade; use bcrypt in production).
- Seats are allocated per-flight and freed on cancellation; double-booking is prevented server-side.
- To reset demo data, stop the server and delete `data/*.json`.
- The desktop app stores its data in `AiroplaneTicketService.app/Contents/app/data/`.
