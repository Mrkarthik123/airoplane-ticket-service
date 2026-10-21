# Airoplane Ticket Service — Presentation Script

*Read the paragraphs below aloud. They are written as continuous sentences, in presentation order.
The whole script is about 6 minutes at a comfortable pace. A 60-second short version is at the end.*

---

## Opening — the problem (about 45 seconds)

Good morning everyone. My project is called **Airoplane Ticket Service**. It is a complete online
flight ticket booking system, built from scratch, that works as a native desktop application, as a web
application, and as an Android application, all from a single set of source code.

Think about what happens when you actually book a flight. You search for a route, you pick a seat, you
enter passenger details, you pay, and then you wait for a ticket. The step that is easy to forget is that
last one. A real airline does not hand you a boarding pass the second your card is charged. A ticketing
officer has to check the booking first, and only after that approval does the ticket become valid. That
single rule is the heart of my project, and I built the whole system around it.

## What the system does (about 90 seconds)

Let me walk you through the customer's journey.

First the customer registers or logs in. When they sign in, they land on a search screen where they choose
a departure airport, an arrival airport, a travel date, and the number of people travelling. The system
returns matching flights with the price, the departure and arrival time, the duration, and the exact number
of seats still free. I seeded the database with real Indian domestic routes, so Delhi to Mumbai, Mumbai to
Bengaluru, and so on all work out of the box.

When the customer chooses a flight, a seat map opens. It is a grid of thirty rows by six seats, one hundred
and eighty seats in total, with an aisle running down the middle. Seats that are already taken are greyed
out and cannot be clicked. Seats that are free turn blue when selected. The system will not let a customer
book more seats than the number of passengers they asked for.

Next comes the passenger form, which asks for the name, the age and the gender of every traveller. The age
field is validated, so it has to be a number between one and one hundred and twenty, and the total price is
recalculated live as the customer picks seats.

After that comes payment, which is a mock gateway. It behaves like a real one, it rejects a card number
that is not twelve to nineteen digits long, and it produces a transaction reference for the record.

And then the important part. The booking is created with the status **PENDING**. The seat is held, so
nobody else can take it, but the customer does not get a valid ticket. They see an amber badge that says
pending, awaiting admin, and a note telling them the ticket will be issued once it is approved.

## The admin approval gate (about 75 seconds)

This is the part of the project I am proudest of, because it is where the design and the real world meet.

The admin logs in and gets a completely separate dashboard, and the last tab is the bookings table. Every
row shows the PNR, the flight, the seats, the total amount, the status and the time it was booked. The
admin selects a pending row and clicks the confirm button. At that moment the status changes to confirmed,
the ticket is issued, the customer's print button unlocks, and the amount is added to the revenue figure.

A few rules are enforced along the way, and I tested all of them. A customer can never confirm their own
booking, they get a forbidden response from the server. The same booking cannot be confirmed twice. A
cancelled booking can never be confirmed. And if the customer cancels a booking that is still pending, the
seat is released immediately so somebody else can take it.

There is a second tab as well, an airports map. I drew the map of India myself using the graphics
functions in Java, and I animated the airport markers so they pulse, and I drew the flight routes between
the cities as lines. Clicking a city fills in the search form for you.

## How it is built (about 90 seconds)

Now the technical side, and this is where I would particularly like feedback.

The unusual decision in this project is that the backend uses **no third-party libraries at all**. I did not
use Spring, I did not use Jackson, I did not use Hibernate. Everything comes from the Java Development
Kit itself. The web server is the built-in HTTP server from the JDK, the JSON parser and serialiser are
written by hand, the password hashing is written by hand, the session store is an in-memory map, and the
graphics and the windows are from Swing.

I know that in industry you would use frameworks, and I would use them too for a large system. But for this
project the constraint forced me to understand what those frameworks do underneath, and it also means the
whole application builds and runs on any machine that has Java 17 installed, with nothing to download.

The code is organised in layers. The model layer holds simple objects for flights, users and bookings. The
service layer holds the actual business rules, and this is the layer that matters, because it is the only
layer that knows what a pending booking means. The store layer is the only part that touches files. And the
user interface layer sits on top.

The really useful consequence of that structure is that the desktop application does not go through the web
server at all. The desktop app calls the service layer directly, in the same process. It is much faster than
sending JSON over a socket and then parsing it back. The web version and the phone version do use the HTTP
API, but they hit the exact same service methods, so the rule that a booking stays pending until an admin
approves it is enforced identically everywhere.

## Languages and technologies (about 60 seconds)

The main language is Java, version 17, and it makes up both the backend and the desktop user interface.
The desktop interface is thirteen Swing classes, including a main window with tabs, a login dialog, a
booking dialog with the seat map, a ticket dialog with printing, and a theme class that keeps all the
colours and fonts consistent.

The web front end is plain HTML, CSS and JavaScript with no framework, so it loads fast and works on a
slow connection. It is also a progressive web app, which means the phone can install it to the home screen
and it keeps working as an app rather than a web page.

The Android version is built with Capacitor, which wraps that same web interface in a native shell, and I
produced an actual installable APK.

The data is stored as JSON files, and passwords are hashed with SHA-256, so no password is ever written to
disk in plain text.

For the Mac version I used the packaging tool that comes with the JDK to build a proper double-clickable
application bundle with its own icon, and I code-signed it so macOS will open it without complaints.

## Testing (about 45 seconds)

I did not just assume the approval flow works, I tested it. I wrote a ten step test at the service level
that walks through creating a booking, checking that it is pending, trying to confirm it as a customer and
being rejected, confirming it as an admin, trying to confirm it twice and being rejected, cancelling a
pending booking and checking that the seat count goes back up. All ten steps pass.

I also tested the real HTTP endpoints against a running server, and I tested the user interface by
rendering the screens to an image and checking the actual pixels, which is how I found and fixed a bug where
the input boxes were white on white and therefore completely invisible.

## Limitations and future work (about 45 seconds)

I want to be honest about the limits. The password hashing has no salt, and in production it should be
bcrypt or Argon2. The data is in files, so it is a single server process and it would not scale to many
users at once. The payment gateway is simulated. And the map is drawn offline rather than streamed from a
live tile server.

If I continued this project, I would add a real payment gateway, seat hold timers that expire if the
customer does not complete payment, email and SMS ticket delivery with a QR code, PDF ticket generation,
and live notifications so the customer sees the moment the admin approves, rather than having to refresh.

## Conclusion (about 30 seconds)

To summarise. Airoplane Ticket Service is a working flight booking system with three front ends and one
shared set of business rules, built entirely on the standard Java platform with no external backend
dependencies. Its central feature, the admin approval gate, models a real airline process: the customer
pays and reserves a seat, but the ticket is only issued once an administrator has approved the booking, and
every transition of that state is validated, persisted, and visible in the user interface.

The thing I took away from building it is that the important part of a booking system is not the search
box or the seat map, it is the state machine around the booking itself, and getting that right is what
makes the rest trustworthy.

Thank you. I am happy to take any questions.

---

## 60-second short version (if you are only given a minute)

My project is **Airoplane Ticket Service**, a flight ticket booking system built in pure Java with no
third-party libraries, delivered as a native desktop app, a web app and an Android app from one codebase.
A customer searches flights, picks a seat on a thirty by six seat map, enters passenger details and pays
through a mock gateway. The key feature is that the booking is created as **pending** and the customer does
not get a valid e-ticket until an administrator confirms it from the admin dashboard, which models how a
real airline issues tickets. The backend is the built-in JDK HTTP server with hand-written JSON, SHA-256
password hashing and file-based storage, the desktop interface is thirteen Swing classes, and the web and
phone front ends call the same service layer, so the approval rule is enforced identically everywhere.
I tested the flow at the service level, over HTTP and by rendering the screens to check the pixels.

---

## Likely questions and short answers

**Why did you not use Spring Boot or a database?**
The aim was to build something that runs anywhere with only a JDK installed, and to see the internals of a
web server, a JSON parser and an ORM instead of just calling them. I would use Spring Boot, JPA and
PostgreSQL if this were a production system, and the layering I used means that swap would not touch the
business rules.

**How is the data stored and is it safe?**
Three JSON files under a data folder, written inside synchronized blocks so concurrent requests cannot
corrupt them, with a concurrent hash map for reads. Passwords are SHA-256 hashed and never stored in plain
text, and sessions are random UUID tokens held in memory.

**What happens if two people try to book the same seat?**
The seat check happens again inside the synchronized block at the moment of booking, so the second person
gets a clear error message saying that the seat was just taken and is asked to pick another. The same
protection exists in the confirmation flow.

**Is the desktop app a real application or a browser window?**
It is a real native window. The desktop app calls the service layer directly in the same process rather
than going through the web server, and it is packaged with the JDK packaging tool into a signed macOS
application bundle with its own icon.

**What is the most difficult part?**
Getting the pending state to behave consistently across three front ends. The fix was to push the rule
into the service layer and have all three call it, rather than checking the status separately in each
screen.
