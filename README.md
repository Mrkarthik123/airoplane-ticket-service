# ✈ Airplane Ticket Service (Java)

Full flight-ticket booking app in **pure Java (no external dependencies)** + HTML/CSS/JS frontend.

- **Search & booking flow** — from/to/date, flight list, seat map (30×6), passengers, mock payment, instant e-ticket with PNR
- **Auth + My bookings** — register/login (token), booking history, view/print ticket, cancel with refund
- **Payments mock** — test card validation, fake `PAY-XXXX` transactions
- **Admin dashboard** — revenue/booking stats, add/edit/delete flights
- **Persistence** — `data/*.json` files (flights, users, bookings)

## 📱 Mobile app (Android APK + PWA)

- **`SkyJet.apk`** — native Android app. Copy to your phone, install, enter
  the server URL (`http://<laptop-ip>:8080`) on first launch. Same Wi-Fi as the laptop.
- **PWA** — on your phone browser open `http://<laptop-ip>:8080` → Add to Home Screen.
  Works on Android + iPhone, runs fullscreen, shell works offline.
- Details + rebuild steps: [`mobile/README.md`](mobile/README.md).

## Run as an app (macOS)

**Option A — double-clickable app (easiest):**
- Double-click **`SkyJet.app`** in Finder (right-click → Open the first time).
- It starts the server and serves the UI at `http://localhost:8080`.

**Option B — double-click launcher:** double-click **`SkyJet.command`**
(it starts the server and opens your browser automatically).

**Option C — portable JAR (works on any OS with Java 17+):**
```bash
java -jar SkyJet.jar
# open http://localhost:8080
```

To rebuild the app files after code changes:
```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java") \
  && cp -r src/main/resources/static/* out/static/ \
  && jar --create --file SkyJet.jar --main-class com.airline.App -C out . \
  && jpackage --name SkyJet --input . --main-jar SkyJet.jar --type app-image --dest . --app-version 1.0.0
```

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
| GET | `/api/admin/stats` | Admin | flights/users/revenue |

## Project layout

```
src/main/java/com/airline/
  App.java                 ← HTTP server + routing (port 8080)
  model/Flight.java, User.java, Booking.java
  store/DataStore.java     ← JSON file DB + seed data
  service/AuthService.java, FlightService.java, BookingService.java
  util/Json.java, HttpUtil.java, Passwords.java
src/main/resources/static/ ← index.html, styles.css, app.js
data/                      ← auto-created flights.json, users.json, bookings.json
```

## Notes

- Passwords are SHA-256 hashed (demo-grade; use bcrypt in production).
- Seats are allocated per-flight and freed on cancellation; double-booking is prevented server-side.
- To reset demo data, stop the server and delete `data/*.json`.
