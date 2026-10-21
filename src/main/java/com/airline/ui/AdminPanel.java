package com.airline.ui;

import com.airline.model.Booking;
import com.airline.model.Flight;
import com.airline.model.User;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import com.airline.store.DataStore;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin tab: stats, flight CRUD and all-bookings management. */
public class AdminPanel extends JPanel {
    public interface OnChanged { void changed(); }

    private final DataStore db;
    private final FlightService flights;
    private final BookingService bookings;
    private final OnChanged onChanged;

    private final JPanel statsRow = new JPanel(new GridLayout(1, 5, 10, 0));
    private final JTable flightTable;
    private final DefaultTableModel flightModel;
    private final JTable bookingTable;
    private final DefaultTableModel bookingModel;
    private final JTextField searchField = Theme.field(20);

    public AdminPanel(DataStore db, FlightService flights, BookingService bookings, OnChanged onChanged) {
        this.db = db;
        this.flights = flights;
        this.bookings = bookings;
        this.onChanged = onChanged;

        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG);
        setBorder(Theme.pad(14, 14, 14, 14));

        statsRow.setOpaque(false);
        add(statsRow, BorderLayout.NORTH);

        flightModel = new DefaultTableModel(new Object[]{"Flight", "Airline", "Route", "Date", "Time", "Price", "Seats"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        flightTable = new JTable(flightModel);
        style(flightTable);

        bookingModel = new DefaultTableModel(new Object[]{"PNR", "Flight", "Seats", "Total", "Status", "Booked at"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(bookingModel);
        style(bookingTable);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.bold(13));
        tabs.addTab("✈ Flights", flightsTab());
        tabs.addTab("🎫 Bookings", bookingsTab());
        add(tabs, BorderLayout.CENTER);
    }

    private void style(JTable t) {
        t.setFont(Theme.font(13));
        t.setRowHeight(26);
        t.setBackground(Color.WHITE);
        t.setGridColor(Theme.LINE);
        t.setSelectionBackground(new Color(0xE0F2FE));
        t.setSelectionForeground(Theme.INK);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    private JPanel flightsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.BG);
        p.setBorder(Theme.pad(12, 12, 12, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(Theme.label("Search"));
        searchField.setPreferredSize(new Dimension(240, 34));
        toolbar.add(searchField);
        JButton doSearch = Theme.button("Search");
        doSearch.addActionListener(e -> refreshFlights());
        JButton clear = Theme.button("Reset");
        clear.addActionListener(e -> { searchField.setText(""); refreshFlights(); });
        toolbar.add(doSearch);
        toolbar.add(clear);
        toolbar.add(Box.createHorizontalStrut(20));
        JButton add = Theme.primary("+ Add flight");
        add.addActionListener(e -> new FlightDialog(SwingUtilities.getWindowAncestor(this), flights, null).setVisible(true));
        toolbar.add(add);
        JButton edit = Theme.button("Edit selected");
        edit.addActionListener(e -> editSelected());
        toolbar.add(edit);
        JButton del = Theme.danger("Delete");
        del.addActionListener(e -> deleteSelected());
        toolbar.add(del);

        JScrollPane sc = new JScrollPane(flightTable);
        sc.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel bookingsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.BG);
        p.setBorder(Theme.pad(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refresh = Theme.button("↻ Refresh");
        refresh.addActionListener(e -> refreshBookings());
        JButton cancel = Theme.danger("Cancel selected booking");
        cancel.addActionListener(e -> cancelSelectedBooking());
        toolbar.add(refresh);
        toolbar.add(cancel);
        JScrollPane sc = new JScrollPane(bookingTable);
        sc.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        p.add(toolbar, BorderLayout.NORTH);
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        int active = 0;
        double revenue = 0;
        for (Booking b : db.bookings.values()) if (b.status.equals("CONFIRMED")) { revenue += b.totalPrice; active++; }
        statsRow.removeAll();
        statsRow.add(stat("Flights", String.valueOf(db.flights.size())));
        statsRow.add(stat("Users", String.valueOf(db.users.size())));
        statsRow.add(stat("Bookings", String.valueOf(db.bookings.size())));
        statsRow.add(stat("Active", String.valueOf(active)));
        statsRow.add(stat("Revenue", "₹" + String.format("%,.0f", revenue)));
        statsRow.revalidate();
        statsRow.repaint();
        refreshFlights();
        refreshBookings();
    }

    private JPanel stat(String label, String value) {
        JPanel p = Theme.card(new GridLayout(0, 1));
        JLabel l = Theme.muted(label, 12);
        JLabel v = Theme.title(value, 20);
        v.setForeground(Theme.PRIMARY_DARK);
        p.add(l);
        p.add(v);
        return p;
    }

    private void refreshFlights() {
        String q = searchField.getText().trim().toLowerCase();
        flightModel.setRowCount(0);
        for (Flight f : flights.search(null, null, null)) {
            String hay = (f.flightNumber + " " + f.airline + " " + f.from + " " + f.to + " " + f.date).toLowerCase();
            if (!q.isEmpty() && !hay.contains(q)) continue;
            flightModel.addRow(new Object[]{f.flightNumber, f.airline, f.from + " → " + f.to, f.date,
                    f.departure + "–" + f.arrival, "₹" + String.format("%,.0f", f.price),
                    f.availableSeats() + "/" + f.totalSeats});
        }
    }

    private void refreshBookings() {
        bookingModel.setRowCount(0);
        for (Booking b : bookings.all()) {
            bookingModel.addRow(new Object[]{b.pnr, b.flightId, String.join(", ", b.seats),
                    "₹" + String.format("%,.0f", b.totalPrice), b.status, b.createdAt});
        }
    }

    private void editSelected() {
        int r = flightTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a flight first."); return; }
        String id = (String) flightTable.getValueAt(r, 0);
        for (Flight f : flights.search(null, null, null)) {
            if (f.flightNumber.equals(id)) { new FlightDialog(SwingUtilities.getWindowAncestor(this), flights, f).setVisible(true); return; }
        }
    }

    private void deleteSelected() {
        int r = flightTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a flight first."); return; }
        String id = (String) flightTable.getValueAt(r, 0);
        for (Flight f : flights.search(null, null, null)) {
            if (!f.flightNumber.equals(id)) continue;
            int ok = JOptionPane.showConfirmDialog(this, "Delete flight " + f.flightNumber + "?",
                    "Delete flight", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            if (!flights.delete(f.id)) {
                JOptionPane.showMessageDialog(this, "Cannot delete: flight has active bookings.");
                return;
            }
            refresh();
            onChanged.changed();
            return;
        }
    }

    private void cancelSelectedBooking() {
        int r = bookingTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a booking first."); return; }
        String pnr = (String) bookingTable.getValueAt(r, 0);
        int ok = JOptionPane.showConfirmDialog(this, "Cancel booking " + pnr + "?", "Cancel booking",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        BookingService.Result res = bookings.cancel(pnr, "", true);
        if (res.error != null) JOptionPane.showMessageDialog(this, res.error);
        refresh();
        onChanged.changed();
    }

    /** Add / edit flight form. */
    static class FlightDialog extends JDialog {
        private final FlightService flights;
        private final Flight editing;
        private final JTextField airline = Theme.field(16);
        private final JTextField number = Theme.field(10);
        private final JTextField from = Theme.field(5);
        private final JTextField to = Theme.field(5);
        private final JTextField date = Theme.field(10);
        private final JTextField dep = Theme.field(6);
        private final JTextField arr = Theme.field(6);
        private final JTextField price = Theme.field(8);

        FlightDialog(java.awt.Window owner, FlightService flights, Flight editing) {
            super(owner, editing == null ? "Add flight" : "Edit flight " + editing.flightNumber, ModalityType.APPLICATION_MODAL);
            this.flights = flights;
            this.editing = editing;
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(Theme.BG);
            setSize(460, 420);
            setLocationRelativeTo(owner);

            JPanel form = Theme.card(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 4, 6, 4);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            y = addRow(form, g, y, "Airline", airline);
            y = addRow(form, g, y, "Flight number", number);
            y = addRow(form, g, y, "From (IATA)", from);
            y = addRow(form, g, y, "To (IATA)", to);
            y = addRow(form, g, y, "Date (yyyy-mm-dd)", date);
            y = addRow(form, g, y, "Departure (HH:mm)", dep);
            y = addRow(form, g, y, "Arrival (HH:mm)", arr);
            y = addRow(form, g, y, "Price (₹)", price);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setOpaque(false);
            JButton cancel = Theme.button("Cancel");
            cancel.addActionListener(e -> dispose());
            JButton save = Theme.primary("Save flight");
            save.addActionListener(e -> save());
            buttons.add(cancel);
            buttons.add(save);

            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);

            if (editing != null) {
                airline.setText(editing.airline);
                number.setText(editing.flightNumber);
                from.setText(editing.from);
                to.setText(editing.to);
                date.setText(editing.date);
                dep.setText(editing.departure);
                arr.setText(editing.arrival);
                price.setText(String.valueOf((long) editing.price));
            } else {
                airline.setText("Airoplane Airways");
                date.setText(java.time.LocalDate.now().plusDays(1).toString());
                dep.setText("09:00");
                arr.setText("11:00");
            }
        }

        private int addRow(JPanel form, GridBagConstraints g, int y, String label, JTextField field) {
            g.gridx = 0; g.gridy = y; g.weightx = 0.35;
            form.add(Theme.label(label), g);
            g.gridx = 1; g.weightx = 0.65;
            form.add(field, g);
            return y + 1;
        }

        private void save() {
            if (airline.getText().isBlank() || number.getText().isBlank() || from.getText().isBlank()
                    || to.getText().isBlank() || date.getText().isBlank() || price.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Fill all required fields.");
                return;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("airline", airline.getText().trim());
            m.put("flightNumber", number.getText().trim().toUpperCase());
            m.put("from", from.getText().trim().toUpperCase());
            m.put("to", to.getText().trim().toUpperCase());
            m.put("date", date.getText().trim());
            m.put("departure", dep.getText().trim());
            m.put("arrival", arr.getText().trim());
            m.put("durationMin", 120);
            m.put("price", Double.parseDouble(price.getText().trim()));
            m.put("totalSeats", 180);
            if (editing == null) flights.create(m);
            else flights.update(editing.id, m);
            dispose();
        }
    }
}
