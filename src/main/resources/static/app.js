/* Airoplane Ticket Service frontend — vanilla JS */
const $ = (s) => document.querySelector(s);
const state = { token: localStorage.getItem('skyjet_token') || '', user: null, flights: [], bookingFlight: null, seats: [], airports: {} };
// API base: same-origin on desktop; configurable for the mobile app (phone talks to laptop over LAN).
const isNativeApp = !!window.Capacitor;
let API = localStorage.getItem('skyjet_api') || '';
function setApiBase(url) { API = (url || '').trim().replace(/\/$/, ''); if (API) localStorage.setItem('skyjet_api', API); else localStorage.removeItem('skyjet_api'); refreshServerBtn(); }

function toast(msg) { const t = $('#toast'); t.textContent = msg; t.style.display = 'block'; setTimeout(() => t.style.display = 'none', 3200); }
function headers(json = true) { const h = {}; if (json) h['Content-Type'] = 'application/json'; if (state.token) h['Authorization'] = 'Bearer ' + state.token; return h; }
async function api(path, opts = {}) { const r = await fetch(API + path, opts); const j = await r.json().catch(() => ({ ok: false, error: 'Bad response' })); if (!r.ok || !j.ok) throw new Error(j.error || ('HTTP ' + r.status)); return j; }
const inr = (n) => '₹' + Number(n || 0).toLocaleString('en-IN');

// ---------- nav ----------
document.querySelectorAll('[data-nav]').forEach(b => b.onclick = () => showView(b.dataset.nav));
function showView(v) {
  document.querySelectorAll('[data-nav]').forEach(b => b.classList.toggle('active', b.dataset.nav === v));
  ['search', 'airports', 'bookings', 'admin'].forEach(k => $('#view-' + k).style.display = k === v ? 'block' : 'none');
  if (v === 'airports') renderAirports();
  if (v === 'bookings') loadMyBookings();
  if (v === 'admin') loadAdmin();
}

// ---------- airports + map (offline SVG, zero dependencies) ----------
const AIRPORT_INFO = {
  DEL: { city: 'New Delhi', name: 'Indira Gandhi Intl', lat: 28.556, lng: 77.100 },
  BOM: { city: 'Mumbai', name: 'Chhatrapati Shivaji Intl', lat: 19.090, lng: 72.865 },
  BLR: { city: 'Bengaluru', name: 'Kempegowda Intl', lat: 13.198, lng: 77.706 },
  HYD: { city: 'Hyderabad', name: 'Rajiv Gandhi Intl', lat: 17.231, lng: 78.429 },
  MAA: { city: 'Chennai', name: 'Chennai Intl', lat: 12.994, lng: 80.171 },
};
let _airportMap = null;
function renderAirports() {
  const codes = Object.keys(AIRPORT_INFO);
  $('#airportList').innerHTML = codes.map(c => {
    const a = AIRPORT_INFO[c];
    return `<div class="card flight" id="airport-card-${c}">
      <div><div class="route">${c} <span class="muted" style="font-size:14px;font-weight:600">· ${a.city}</span></div>
      <div class="meta">${a.name} · ${a.lat.toFixed(3)}°N, ${a.lng.toFixed(3)}°E</div></div>
      <div class="fare"><button class="btn btn-sm" onclick="flyFrom('${c}')">Fly from here</button></div>
    </div>`;
  }).join('');
  const el = $('#indiaMap');
  if (!el) return;
  if (typeof L === 'undefined') { el.innerHTML = '<div class="card">Real map needs an internet connection.</div>'; return; }
  if (_airportMap) { _airportMap.remove(); _airportMap = null; }
  const map = L.map('indiaMap').setView([22.5, 79.5], 5);
  _airportMap = map;
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' }).addTo(map);
  const latlngs = codes.map(c => {
    const a = AIRPORT_INFO[c];
    const ll = [a.lat, a.lng];
    L.marker(ll).addTo(map).bindPopup(`<b>${c} — ${a.city}</b><br>${a.name}`);
    return ll;
  });
  for (let i = 0; i < latlngs.length; i++) for (let j = i + 1; j < latlngs.length; j++) {
    L.polyline([latlngs[i], latlngs[j]], { color: '#0ea5e9', weight: 1.5, dashArray: '5 4', opacity: 0.7 }).addTo(map);
  }
  map.fitBounds(L.latLngBounds(latlngs).pad(0.3));
  setTimeout(() => map.invalidateSize(), 100);
}
window.flyFrom = (c) => {
  showView('search');
  if ($('#fromSel')) $('#fromSel').value = c;
};
document.querySelectorAll('[data-close]').forEach(b => b.onclick = () => $('#' + b.dataset.close).style.display = 'none');

// ---------- auth ----------
let mode = 'login';
$('#authBtn').onclick = () => $('#authModal').style.display = 'flex';
$('#tabLogin').onclick = () => setMode('login');
$('#tabReg').onclick = () => setMode('register');
function setMode(m) { mode = m; $('#tabLogin').classList.toggle('active', m === 'login'); $('#tabReg').classList.toggle('active', m === 'register'); $('#aName').style.display = m === 'register' ? 'block' : 'none'; $('#authTitle').textContent = m === 'login' ? 'Login' : 'Create account'; }
$('#authForm').onsubmit = async (e) => {
  e.preventDefault();
  try {
    const body = { email: $('#aEmail').value.trim(), password: $('#aPass').value };
    let j;
    if (mode === 'register') { body.name = $('#aName').value.trim(); j = await api('/api/auth/register', { method: 'POST', headers: headers(), body: JSON.stringify(body) }); }
    else j = await api('/api/auth/login', { method: 'POST', headers: headers(), body: JSON.stringify(body) });
    state.token = j.token; state.user = j.user; localStorage.setItem('skyjet_token', state.token);
    $('#authModal').style.display = 'none'; refreshAuth(); toast('Welcome, ' + state.user.name + '!');
    if (state.user.role === 'admin') showView('admin');
  } catch (err) { toast(err.message); }
};
function refreshAuth() {
  const logged = !!state.user;
  $('#authBtn').textContent = logged ? 'Logout' : 'Login';
  $('#userChip').style.display = logged ? 'inline' : 'none';
  if (logged) $('#userChip').textContent = state.user.name + (state.user.role === 'admin' ? ' (admin)' : '');
  $('#adminNav').style.display = state.user && state.user.role === 'admin' ? 'block' : 'none';
}
$('#authBtn').onclick = () => {
  if (state.user) { state.user = null; state.token = ''; localStorage.removeItem('skyjet_token'); refreshAuth(); toast('Logged out.'); }
  else $('#authModal').style.display = 'flex';
};
// ---------- server switch (used by mobile app on LAN) ----------
function refreshServerBtn() { const b = $('#serverBtn'); if (b) b.title = 'Server: ' + (API || location.origin + ' (this device)'); }
$('#serverBtn').onclick = () => {
  const cur = API || '';
  const v = prompt('Airoplane Ticket Service server URL (empty = this device).\nOn your phone use your laptop IP, e.g. http://192.168.1.5:8080', cur);
  if (v === null) return;
  setApiBase(v);
  state.flights = [];
  init(true);
  toast(API ? 'Server set to ' + API : 'Using built-in server.');
};
async function restoreSession() {
  if (!state.token) return;
  try { const j = await api('/api/auth/me', { headers: headers(false) }); state.user = j.user; refreshAuth(); } catch { state.token = ''; localStorage.removeItem('skyjet_token'); }
}

// ---------- search ----------
function fillAirports() {
  const a = state.airports;
  const opts = Object.entries(a).map(([c, n]) => `<option value="${c}">${c} — ${n.split('(')[0].trim()}</option>`).join('');
  $('#fromSel').innerHTML = opts; $('#toSel').innerHTML = opts;
  $('#fromSel').value = 'DEL'; $('#toSel').value = 'BOM';
  if (!$('#dateInp').value) {
    const t = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    $('#dateInp').value = t;
  }
  $('#dateInp').min = new Date().toISOString().slice(0, 10);
}
$('#swapBtn').onclick = () => { const v = $('#fromSel').value; $('#fromSel').value = $('#toSel').value; $('#toSel').value = v; };
$('#searchForm').onsubmit = async (e) => {
  e.preventDefault();
  const from = $('#fromSel').value, to = $('#toSel').value, date = $('#dateInp').value;
  if (from === to) return toast('From and To cannot be the same.');
  try {
    const j = await api(`/api/flights?from=${from}&to=${to}&date=${date}`, { headers: headers(false) });
    state.airports = j.airports || state.airports; fillKeep();
    state.flights = j.flights;
    renderResults();
  } catch (err) { toast(err.message); }
};
function fillKeep() { const f = $('#fromSel').value, t = $('#toSel').value; fillAirports(); $('#fromSel').value = f; $('#toSel').value = t; }
function renderResults() {
  const box = $('#results');
  if (!state.flights.length) { box.innerHTML = `<div class="card">No flights found. Try another date or route.</div>`; return; }
  box.innerHTML = state.flights.map(f => `
    <div class="card flight">
      <div><div class="route">${f.from} → ${f.to}</div>
      <div class="meta">${f.airline} · ${f.flightNumber} · ${f.date} · ${f.departure}–${f.arrival} · ${Math.floor(f.durationMin / 60)}h ${f.durationMin % 60}m</div>
      <span class="pill ${f.availableSeats > 20 ? 'green' : 'red'}">${f.availableSeats} seats left</span></div>
      <div class="fare"><b>${inr(f.price)}</b><div class="muted small">per adult</div>
      <button class="btn btn-primary" onclick="startBooking('${f.id}')">Book now</button></div>
    </div>`).join('');
}

// ---------- booking ----------
window.startBooking = async (id) => {
  const f = state.flights.find(x => x.id === id) || (await api('/api/flights/' + id, { headers: headers(false) })).flight;
  const need = parseInt($('#paxSel').value || '1', 10);
  state.bookingFlight = f; state.seats = [];
  $('#bookTitle').textContent = `${f.airline} ${f.flightNumber} · ${f.from}→${f.to}`;
  $('#seatNeed').textContent = need;
  renderSeatMap(f); renderPax(need); updatePrice();
  $('#bookModal').style.display = 'flex';
};
function renderSeatMap(f) {
  const taken = new Set(f.bookedSeats || []);
  let html = '';
  for (let row = 1; row <= 30; row++) {
    for (const col of ['A', 'B', 'C', '', 'D', 'E', 'F']) {
      if (!col) { html += `<div class="seat aisle">${row}</div>`; continue; }
      const s = row + col;
      html += `<div class="seat ${taken.has(s) ? 'taken' : ''}" data-seat="${s}" onclick="toggleSeat('${s}',this)">${s}</div>`;
    }
  }
  $('#seatMap').innerHTML = html;
}
window.toggleSeat = (s, el) => {
  if (el.classList.contains('taken')) return;
  const need = parseInt($('#paxSel').value || '1', 10);
  if (el.classList.contains('sel')) { el.classList.remove('sel'); state.seats = state.seats.filter(x => x !== s); }
  else {
    if (state.seats.length >= need) return toast(`Select only ${need} seat(s).`);
    el.classList.add('sel'); state.seats.push(s);
  }
  updatePrice();
};
function renderPax(n) {
  $('#paxForms').innerHTML = Array.from({ length: n }, (_, i) => `
    <div class="stack" style="margin-bottom:8px"><b>Passenger ${i + 1}</b>
    <input data-pax="${i}" data-k="name" placeholder="Full name" required />
    <div class="row" style="margin:0"><input data-pax="${i}" data-k="age" type="number" min="1" max="120" placeholder="Age" style="flex:1" required />
    <select data-pax="${i}" data-k="gender" style="flex:1"><option>Male</option><option>Female</option><option>Other</option></select></div></div>`).join('');
}
function updatePrice() {
  const n = parseInt($('#paxSel').value || '1', 10);
  $('#priceLine').textContent = `Total: ${inr((state.bookingFlight?.price || 0) * n)} · ${n} ${n === 1 ? 'person' : 'people'} · Seats: ${state.seats.join(', ') || '—'}`;
}
$('#payBtn').onclick = async () => {
  try {
    if (!state.user) { $('#bookModal').style.display = 'none'; $('#authModal').style.display = 'flex'; return toast('Login first to book.'); }
    const n = parseInt($('#paxSel').value || '1', 10);
    if (state.seats.length !== n) return toast(`Please select ${n} seat(s).`);
    const pax = [];
    for (let i = 0; i < n; i++) {
      const name = document.querySelector(`[data-pax="${i}"][data-k="name"]`).value.trim();
      const age = document.querySelector(`[data-pax="${i}"][data-k="age"]`).value;
      const gender = document.querySelector(`[data-pax="${i}"][data-k="gender"]`).value;
      if (!name || !age) return toast('Fill all passenger details.');
      pax.push({ name, age: parseInt(age, 10), gender });
    }
    const card = $('#cardInp').value.replace(/\s/g, '');
    const amount = state.bookingFlight.price * n;
    const pay = await api('/api/payments/mock', { method: 'POST', headers: headers(), body: JSON.stringify({ amount, cardNumber: card }) });
    const j = await api('/api/bookings', { method: 'POST', headers: headers(), body: JSON.stringify({ flightId: state.bookingFlight.id, passengers: pax, seats: state.seats, paymentId: pay.payment.paymentId, paidAmount: amount }) });
    $('#bookModal').style.display = 'none';
    showTicket(j.booking);
    toast('Booking confirmed! PNR ' + j.booking.pnr);
  } catch (err) { toast(err.message); }
};

// ---------- ticket + my bookings ----------
function showTicket(b) {
  const f = b.flight || {};
  $('#ticketBody').innerHTML = `<div class="ticket">
    <div class="muted small">SKYJET E-TICKET · ${b.status}</div><div class="pnr">${b.pnr}</div>
    <h2>${f.from || ''} → ${f.to || ''}</h2>
    <p>${f.airline || ''} ${f.flightNumber || ''} · ${f.date || ''} · ${f.departure || ''}–${f.arrival || ''}</p>
    <p><b>Passengers:</b> ${(b.passengers || []).map(p => `${p.name} (${p.age})`).join(', ')}</p>
    <p><b>Seats:</b> ${(b.seats || []).join(', ')} · <b>Total paid:</b> ${inr(b.totalPrice)}</p>
    <p class="muted small">Payment ${b.paymentId || ''} · Booked ${String(b.createdAt || '').slice(0, 16).replace('T', ' ')}</p></div>`;
  $('#ticketModal').style.display = 'flex';
}
async function loadMyBookings() {
  const box = $('#myBookings');
  if (!state.user) { box.innerHTML = `<div class="card">Please <b>Login</b> to view bookings.</div>`; return; }
  try {
    const j = await api('/api/bookings', { headers: headers(false) });
    if (!j.bookings.length) { box.innerHTML = `<div class="card">No bookings yet. Search and book your first flight!</div>`; return; }
    box.innerHTML = j.bookings.map(b => { const f = b.flight || {}; return `
      <div class="card flight"><div><div class="route">${f.from || '?'} → ${f.to || '?'} <span class="pill ${b.status === 'CONFIRMED' ? 'green' : 'red'}">${b.status}</span></div>
      <div class="meta">PNR <b>${b.pnr}</b> · ${f.airline || ''} ${f.flightNumber || ''} · ${f.date || ''} · Seats ${(b.seats || []).join(', ')} · ${inr(b.totalPrice)}</div></div>
      <div class="fare"><button class="btn btn-sm" onclick='showTicket(${JSON.stringify(b).replace(/'/g, "&#39;")})'>View ticket</button>
      ${b.status === 'CONFIRMED' ? `<button class="btn btn-sm" onclick="cancelBooking('${b.pnr}')">Cancel</button>` : ''}</div></div>`; }).join('');
    window._cache = j.bookings;
    window.showTicket = (b) => { if (typeof b === 'string') b = window._cache.find(x => x.pnr === b) || b; if (typeof b !== 'object') return; showTicket(b); };
  } catch (err) { box.innerHTML = `<div class="card">${err.message}</div>`; }
}
window.cancelBooking = async (pnr) => {
  if (!confirm('Cancel booking ' + pnr + '? Refund will be mocked instantly.')) return;
  try { await api('/api/bookings/' + pnr + '/cancel', { method: 'POST', headers: headers() }); toast('Booking ' + pnr + ' cancelled.'); loadMyBookings(); }
  catch (err) { toast(err.message); }
};

// ---------- admin ----------
let adminTab = 'flights';
let adminFlightFilter = '';
let adminBookingFilter = '';
let adminAllBookings = [];
function setAdminTab(t) {
  adminTab = t;
  const tf = $('#tabFlights'), tb = $('#tabBookings');
  if (tf) tf.classList.toggle('active', t === 'flights');
  if (tb) tb.classList.toggle('active', t === 'bookings');
  if ($('#adminFlightsPane')) $('#adminFlightsPane').style.display = t === 'flights' ? 'block' : 'none';
  if ($('#adminBookingsPane')) $('#adminBookingsPane').style.display = t === 'bookings' ? 'block' : 'none';
  if (t === 'bookings') loadAdminBookings();
}
async function loadAdmin() {
  if (!state.user || state.user.role !== 'admin') {
    if ($('#adminGate')) $('#adminGate').innerHTML = '<div class="card">Admin login required. Please log in with an admin account.</div>';
    if ($('#adminBody')) $('#adminBody').style.display = 'none';
    return;
  }
  if ($('#adminGate')) $('#adminGate').innerHTML = '';
  if ($('#adminBody')) $('#adminBody').style.display = 'block';
  try {
    const s = await api('/api/admin/stats', { headers: headers(false) });
    $('#statsRow').innerHTML = [['Flights', s.stats.flights], ['Users', s.stats.users], ['Bookings', s.stats.bookings], ['Active', s.stats.activeBookings], ['Revenue', inr(s.stats.revenue)]].map(([k, v]) => `<div class="stat"><span class="muted">${k}</span><b>${v}</b></div>`).join('');
    const f = await api('/api/flights', { headers: headers(false) });
    state.flights = f.flights;
    renderAdminFlights();
    if (adminTab === 'bookings') loadAdminBookings();
  } catch (err) { toast(err.message); }
}
function renderAdminFlights() {
  const q = (adminFlightFilter || '').toLowerCase();
  const list = (state.flights || []).filter(x => !q || [x.flightNumber, x.airline, x.from, x.to, x.date].join(' ').toLowerCase().includes(q));
  $('#adminFlights').innerHTML = list.length ? `<div class="card"><table><tr><th>Flight</th><th>Route</th><th>Date</th><th>Time</th><th>Price</th><th>Seats</th><th></th></tr>${list.map(x => `<tr><td>${x.airline} <b>${x.flightNumber}</b></td><td>${x.from}→${x.to}</td><td>${x.date}</td><td>${x.departure}-${x.arrival}</td><td>${inr(x.price)}</td><td>${x.availableSeats}/${x.totalSeats}</td><td><button class="btn btn-sm" onclick="editFlight('${x.id}')">Edit</button> <button class="btn btn-sm" onclick="delFlight('${x.id}')">Del</button></td></tr>`).join('')}</table></div>`
    : '<div class="card">No flights match.</div>';
}
async function loadAdminBookings() {
  try {
    const j = await api('/api/bookings', { headers: headers() });
    adminAllBookings = j.bookings || [];
    renderAdminBookings();
  } catch (err) { if ($('#adminBookings')) $('#adminBookings').innerHTML = `<div class="card">${err.message}</div>`; }
}
function renderAdminBookings() {
  const q = (adminBookingFilter || '').toLowerCase();
  const list = adminAllBookings.filter(b => !q || [b.pnr, b.flightId, b.status, b.userId].join(' ').toLowerCase().includes(q));
  if (!$('#adminBookings')) return;
  $('#adminBookings').innerHTML = list.length ? `<div class="card"><table><tr><th>PNR</th><th>Flight</th><th>Seats</th><th>Total</th><th>Status</th><th></th></tr>${list.map(b => `<tr><td><b>${b.pnr}</b></td><td>${b.flightId}</td><td>${(b.seats || []).join(', ')}</td><td>${inr(b.totalPrice)}</td><td><span class="pill ${b.status === 'CONFIRMED' ? 'green' : 'red'}">${b.status}</span></td><td>${b.status === 'CONFIRMED' ? `<button class="btn btn-sm" onclick="adminCancelBooking('${b.pnr}')">Cancel</button>` : ''}</td></tr>`).join('')}</table></div>`
    : '<div class="card">No bookings found.</div>';
}
window.adminCancelBooking = async (pnr) => {
  if (!confirm('Cancel booking ' + pnr + ' (admin)?')) return;
  try { await api('/api/bookings/' + pnr + '/cancel', { method: 'POST', headers: headers() }); toast('Booking ' + pnr + ' cancelled.'); loadAdminBookings(); loadAdmin(); } catch (err) { toast(err.message); }
};
window.editFlight = (id) => {
  const f = state.flights.find(x => x.id === id); if (!f) return;
  $('#flightFormTitle').textContent = 'Edit flight ' + f.flightNumber;
  $('#fId').value = f.id; $('#fAirline').value = f.airline; $('#fNum').value = f.flightNumber;
  $('#fFrom').value = f.from; $('#fTo').value = f.to; $('#fDate').value = f.date;
  $('#fDep').value = f.departure; $('#fArr').value = f.arrival; $('#fPrice').value = f.price;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};
window.delFlight = async (id) => {
  if (!confirm('Delete this flight?')) return;
  try { await api('/api/flights/' + id, { method: 'DELETE', headers: headers() }); toast('Flight deleted.'); loadAdmin(); } catch (err) { toast(err.message); }
};
$('#fReset').onclick = () => { $('#flightForm').reset(); $('#fId').value = ''; $('#flightFormTitle').textContent = 'Add flight'; };
if ($('#tabFlights')) $('#tabFlights').onclick = () => setAdminTab('flights');
if ($('#tabBookings')) $('#tabBookings').onclick = () => setAdminTab('bookings');
if ($('#adminSearchBtn')) $('#adminSearchBtn').onclick = () => { adminFlightFilter = $('#adminSearch').value.trim(); renderAdminFlights(); };
if ($('#adminResetBtn')) $('#adminResetBtn').onclick = () => { adminFlightFilter = ''; $('#adminSearch').value = ''; renderAdminFlights(); };
if ($('#adminSearch')) $('#adminSearch').onkeydown = (e) => { if (e.key === 'Enter') { e.preventDefault(); adminFlightFilter = e.target.value.trim(); renderAdminFlights(); } };
if ($('#bookingSearchBtn')) $('#bookingSearchBtn').onclick = () => { adminBookingFilter = $('#bookingSearch').value.trim(); renderAdminBookings(); };
if ($('#bookingResetBtn')) $('#bookingResetBtn').onclick = () => { adminBookingFilter = ''; $('#bookingSearch').value = ''; renderAdminBookings(); };
if ($('#bookingRefreshBtn')) $('#bookingRefreshBtn').onclick = () => loadAdminBookings();
$('#flightForm').onsubmit = async (e) => {
  e.preventDefault();
  const body = { airline: $('#fAirline').value, flightNumber: $('#fNum').value, from: $('#fFrom').value.toUpperCase(), to: $('#fTo').value.toUpperCase(), date: $('#fDate').value, departure: $('#fDep').value, arrival: $('#fArr').value, durationMin: 120, price: parseFloat($('#fPrice').value), totalSeats: 180 };
  try {
    const id = $('#fId').value;
    if (id) await api('/api/flights/' + id, { method: 'PUT', headers: headers(), body: JSON.stringify(body) });
    else await api('/api/flights', { method: 'POST', headers: headers(), body: JSON.stringify(body) });
    toast('Flight saved.'); $('#flightForm').reset(); $('#fId').value = ''; loadAdmin();
  } catch (err) { toast(err.message); }
};

// ---------- init ----------
async function init() {
  refreshServerBtn();
  if (isNativeApp && !API) {
    const v = prompt('Enter your Airoplane Ticket Service server URL\n(same Wi-Fi as your laptop, e.g. http://192.168.1.5:8080)', '');
    if (v) setApiBase(v);
  }
  try {
    const j = await api('/api/flights', { headers: headers(false) });
    state.airports = j.airports; fillAirports();
    const t = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    const j2 = await api(`/api/flights?from=DEL&to=BOM&date=${t}`, { headers: headers(false) });
    state.flights = j2.flights; renderResults();
  } catch (err) { $('#results').innerHTML = `<div class="card">Backend not reachable: ${err.message}. ${isNativeApp ? 'Tap ⚙ to set the server URL.' : 'Start it with <code>./run.sh</code>.'}</div>`; }
  await restoreSession();
}
init();
