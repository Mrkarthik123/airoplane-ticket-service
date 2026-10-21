# PROJECT ABSTRACT

---

### ✈ AIROPLANE TICKET SERVICE

**A Flight Reservation System with a Native Desktop Application, Web Interface and Android Client**

---

| | |
|---|---|
| **Project Title** | Airoplane Ticket Service — Flight Reservation & E-Ticket System |
| **Team / Student Name** | ______________________________ |
| **Roll Number** | ______________________________ |
| **Class / Section** | ______________________________ |
| **Guide / Faculty** | ______________________________ |
| **Department** | Computer Science & Engineering |
| **College / Institution** | ______________________________ |
| **Academic Year** | 2025 – 2026 |
| **Date of Submission** | ______________________________ |

---

## ABSTRACT

The airline reservation system developed in this project automates the complete flight booking workflow,
from route search to the issuance of a valid e-ticket, in a single application that runs as a native
desktop program, as a web interface and as an Android application. It is written in pure Java using only
the facilities of the Java Development Kit, and therefore contains no third-party runtime dependencies.

The working begins when a customer registers or signs in, selects a source airport, destination airport,
travel date and number of passengers, and searches for matching flights. Each result displays the airline,
flight number, schedule, duration, fare and the exact number of seats still available. On selecting a
flight, the customer is shown an interactive seat map of thirty rows and six seats, in which occupied
seats are disabled and chosen seats are highlighted; the system prevents the selection of more seats than
the number of passengers declared. The passenger details, namely name, age and gender, are collected for
every traveller, with the age validated as a number between one and one hundred and twenty. A simulated
payment gateway validates the test card and returns a transaction reference, after which the booking is
recorded with a unique six-character passenger reference number.

The central design decision of this project is the introduction of an administrative approval stage. Every
new booking is given the status of pending. The seat is reserved immediately so that it cannot be sold
twice, but no valid e-ticket is issued at this stage; the interface marks the booking as awaiting approval
and disables printing. An administrator signs in to a separate dashboard, reviews the pending bookings,
and confirms or cancels the selected booking. Only upon confirmation does the booking change to the
confirmed state, the e-ticket become valid and printable, and the amount be recorded in the revenue
figures. A customer is technically prevented from confirming their own booking, a booking cannot be
confirmed twice, a cancelled booking can never be confirmed, and cancelling a pending booking releases
the seat back to the system.

The backend is built on the HTTP server supplied with the Java Development Kit, and is strengthened by a
hand-written JSON parser and serialiser, SHA-256 password hashing, token-based session management and
thread-safe file-based persistence. The data is organised in four layers, namely the model, service,
storage and user-interface layers, which allows the identical business rules to be enforced by the
desktop, web and mobile applications alike. The desktop application is built with thirteen Java Swing
classes and is packaged into a signed, double-clickable macOS bundle; the web interface, written in
plain HTML, CSS and JavaScript, is an installable progressive web application; and the Android client is
produced with the Capacitor framework.

The system was validated through a ten-stage test at the service level, a live test of the HTTP endpoints
against a running server, and a rendering test of the user interface in which the actual pixels of the
drawn screens were inspected. The completed application satisfies the objectives stated below and
demonstrates a practical understanding of layered software design, secure authentication, state-managed
business workflows and the delivery of one shared set of business rules to multiple user interfaces.

**Keywords:** Java, Swing, HTTP server, JSON, SHA-256 authentication, seat reservation, administrative
approval workflow, e-ticket, progressive web application, Capacitor, file-based persistence.

---

## 1. INTRODUCTION

Airline reservation is a process that every traveller is familiar with, yet the underlying software is
rarely visible to the end user. The visible outcome is a small document containing a passenger reference
number, a date, a seat and a list of names, but the journey that produces it involves searching a live
inventory, holding a limited and perishable resource, collecting the personal details of several
travellers, taking a payment and finally authorising the travel.

The system presented in this project reproduces that journey in a form that is complete enough to be
useful and small enough to be entirely understandable. Every part of it was written for this project,
including the HTTP server, the JSON handling, the password hashing and the graphical interface, so that
the behaviour of the application at every stage is a deliberate outcome rather than an inherited default.

A further consideration shaped the design. Booking systems are frequently demonstrated as a single
interface that confirms a ticket the instant a payment succeeds, which is convenient for a demonstration
but inaccurate with respect to the real process. Airline staff review a booking before a ticket is
released, and the administrator of an online travel portal is no different. This project therefore places
the confirmation step deliberately between the payment and the ticket, and makes that step the
organising principle of the entire application.

## 2. OBJECTIVES

1. To design and develop a complete flight reservation system covering search, seat selection, passenger
   details, payment, approval, e-ticket generation and cancellation.
2. To build the server on the standard Java platform alone, avoiding third-party frameworks, so that the
   application is transparent, self-contained and runnable on any machine with Java 17 installed.
3. To enforce a realistic administrative approval workflow in which an e-ticket is issued only after a
   booking has been reviewed and approved by an authorised administrator.
4. To reuse a single set of business rules across a native desktop application, a responsive web
   interface and an Android application.
5. To protect user credentials by storing only hashed passwords and to manage access through expiring
   token-based sessions.
6. To persist all operational data in a simple, portable and human-readable form that requires no database
   server to install or operate the project.
7. To package the desktop application as a self-contained, double-clickable bundle that requires no
   additional configuration on the user's machine.

## 3. SYSTEM DESIGN

The application is organised into four layers, with each layer permitted to depend only on the layer
beneath it.

**Model layer.** Three plain classes, namely Flight, User and Booking, describe the entities of the
system. Each class is responsible for converting itself to and from the JSON representation used for
storage and for transfer across the network.

**Service layer.** This layer contains the business rules and is the heart of the system. The
AuthService performs registration, login, password verification and session lookup. The FlightService
performs the search and the creation, modification and removal of flights, and reports seat
availability. The BookingService creates bookings, holds seats, applies the approval rule, issues and
withdraws tickets, and performs the cancellation with its refund. The service layer is independent of
whether it is being used by a desktop window, a browser or a phone.

**Storage layer.** A single DataStore class reads and writes the three JSON files that hold flights,
users and bookings. All modifications take place inside synchronised blocks and the in-memory tables are
concurrent hash maps, so that simultaneous requests cannot corrupt the files or lose an update. On its
first run the store seeds a set of Indian domestic flights together with an administrator account and a
customer account, and it advances the dates of the seeded flights so that the demonstration data always
remains relevant.

**Interface layer.** The desktop interface consists of thirteen Java Swing classes, which include the
main window with its tabs, the login and registration dialog, the search panel, the airports panel with
its painted map, the booking dialog containing the seat map, the ticket dialog with printing support, the
bookings panel and the administrator panel. A shared theme class holds the colours, fonts and factories
for the individual controls. The web interface consists of a single HTML file, a style sheet and a script
file, and the mobile application reuses that same script file inside a native shell.

The desktop application communicates with the service layer directly and in the same process, so that no
data has to be serialised into JSON and parsed back out again. The web and mobile clients communicate
through the JSON API exposed by the server, and both routes therefore reach the identical business rules.

## 4. LANGUAGES AND TECHNOLOGIES USED

**Java 17 is the principal language of the project.** Twenty-four source files containing approximately
three thousand lines of Java implement the server, the business logic, the storage mechanism and the
complete desktop graphical interface. The standard library supplies the HTTP server, the concurrency
utilities, the cryptography and the Swing toolkit. The Swing classes use custom painting to draw the
animated sky background and the map of India, custom borders and colour schemes for the seat map, and
the printing API for the e-ticket.

**HTML, CSS and JavaScript form the web interface.** The style sheet implements a responsive layout with
a glass-morphism appearance, a grid-based seat map and modal dialogs. The script file uses the fetch
interface to call the API, maintains the session token in the browser's local storage, renders the flight
results, drives the booking and administrator workflows, and registers a service worker that makes the
application installable and cacheable on a mobile device.

**JSON is used both as the storage format and as the format of every message exchanged between the
client and the server.** A dedicated utility class performs the parsing and the serialisation, so that no
external library is required for this purpose.

**Supporting tools.** Maven describes the project and produces a runnable archive, and a shell script
compiles and starts the application using only the Java Development Kit. The Android client is produced
with the Capacitor framework and the Gradle build system, and the macOS application bundle is produced
with the packaging tool of the Java Development Kit and signed locally so that it opens without any
warning on the user's machine.

## 5. SYSTEM MODULES

**Authentication module.** Handles registration and login, verifies the SHA-256 hash of the supplied
password against the stored hash, and issues a random session token on success. A customer account and an
administrator account are created automatically on the first run.

**Flight search module.** Accepts the source airport, the destination airport and the date, and returns
the matching flights with their schedule, duration, fare and remaining seats, along with the list of
served airports.

**Seat reservation module.** Renders a map of thirty rows by six seats with a central aisle, marks the
occupied seats, records the seats chosen by the customer, and rejects a selection that exceeds the number
of passengers. The availability of a seat is checked again at the moment the booking is written, so that
two customers cannot acquire the same seat.

**Passenger and payment module.** Collects the name, age and gender of every traveller and validates the
entries. The payment module behaves as a mock gateway, validating the test card number, recording the
amount and the last four digits, and returning a transaction reference.

**Approval and ticketing module.** Creates the booking in the pending state, holds the seat, and exposes
the confirmation and cancellation actions to the administrator. On confirmation it issues the e-ticket
containing the passenger reference number, the flight details, the passenger table with the assigned
seats and the total amount, and enables printing. On cancellation it releases the seats and records a
mock refund.

**Administrator module.** Displays live statistics covering the number of flights, users, bookings,
active bookings, pending bookings and revenue, permits the creation, modification and removal of
flights, and provides the list of all bookings with the actions to confirm a pending booking or cancel
one.

**Interface modules.** The desktop window, the web interface and the mobile client present the same
functions to the customer and to the administrator, and the airports module displays the network of
served cities on an animated map.

## 6. TESTING PERFORMED

The system was tested at three levels. At the service level, a ten-stage procedure creates a booking and
verifies that it is pending, verifies that confirmation by a customer is refused, verifies that
confirmation by an administrator produces the confirmed state and records the time of confirmation,
verifies that a second confirmation is refused, verifies that a cancelled booking cannot be confirmed,
and verifies that cancelling a pending booking returns the seat to the available pool.

At the level of the HTTP interface, the running server was queried directly. The creation of a booking
returned the pending status, a confirmation attempted with a customer token was refused with a forbidden
response, a confirmation performed with an administrator token returned the confirmed status with a
confirmation timestamp, and the statistics endpoint reported the number of pending bookings correctly.

At the level of the interface, the screens were rendered to an image and the actual pixels were
examined. This procedure confirmed the presence of the pending status badge, the warning notice and the
disabled print control, and it also detected and led to the correction of a defect in which the input
boxes of the login screen were drawn in white on a white background and were therefore invisible to the
user.

## 7. LIMITATIONS AND FUTURE ENHANCEMENTS

The present implementation carries certain limitations that are acknowledged openly. The password
hashing applies SHA-256 without a salt, and a production system would employ bcrypt or Argon2. The
data is held in files, so the application runs as a single process and does not support several server
instances sharing one database. The payment gateway is simulated, and the map of India is drawn by the
program rather than assembled from an online tile service.

The work that would follow naturally includes the integration of a real payment gateway, the expiry of
held seats after a fixed period, the delivery of the ticket by e-mail and short message with a QR code
for validation at the gate, the generation of the e-ticket as a portable document, support for several
airlines with distinct fare classes and dynamic pricing, the delivery of an instant notification to the
customer at the moment the booking is approved, and the migration of the storage to a conventional
relational database.

## 8. CONCLUSION

The project delivers a working airline reservation system that is complete in its behaviour, transparent
in its construction and portable in its deployment. Its three client applications, namely the native
desktop program, the web interface and the Android client, share a single set of business rules enforced
in one service layer, which guarantees that a booking behaves identically wherever it is examined. The
administrative approval stage places the accuracy of the system above the convenience of a demonstration,
since a customer may reserve and pay for a seat but receives no valid ticket until an authorised
administrator has reviewed the booking. The completed work demonstrates a practical understanding of
layered software design, of the handling of security in an application, of the modelling of a business
workflow as an explicit sequence of states, and of the delivery of that workflow to several user
interfaces without duplicating the rules that enforce it.

---

### **Repository:** `https://github.com/Mrkarthik123/airoplane-ticket-service`

### **Demonstration accounts**

| Role | E-mail | Password |
|---|---|---|
| Customer | `demo@skyjet.in` | `demo123` |
| Administrator | `admin@skyjet.in` | `admin123` |

---

*End of abstract.*
