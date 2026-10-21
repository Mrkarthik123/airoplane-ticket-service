package com.airline.ui;

import com.airline.model.Booking;
import com.airline.model.Flight;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** "My bookings" tab: list of bookings with view-ticket and cancel actions. */
public class BookingsPanel extends JPanel {
    public interface OnChanged { void changed(); }

    private final BookingService bookings;
    private final FlightService flights;
    private final String userId;
    private final boolean admin;
    private final OnChanged onChanged;
    private final JPanel list = new JPanel();
    private final JLabel count = Theme.muted("", 12);

    public BookingsPanel(BookingService bookings, FlightService flights, String userId, boolean admin, OnChanged onChanged) {
        this.bookings = bookings;
        this.flights = flights;
        this.userId = userId;
        this.admin = admin;
        this.onChanged = onChanged;

        setLayout(new BorderLayout(0, 10));
        setBackground(Theme.BG);
        setBorder(Theme.pad(14, 14, 14, 14));

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Theme.title(admin ? "All bookings" : "My bookings", 17), BorderLayout.WEST);
        head.add(count, BorderLayout.EAST);

        JScrollPane sc = new JScrollPane(list);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(head, BorderLayout.NORTH);
        center.add(sc, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void refresh() {
        list.removeAll();
        List<Booking> all = admin ? bookings.all() : bookings.forUser(userId);
        count.setText(all.size() + " booking" + (all.size() == 1 ? "" : "s"));
        if (all.isEmpty()) {
            JPanel empty = Theme.card(new BorderLayout());
            empty.add(Theme.muted("No bookings yet. Search a flight and book a seat.", 13), BorderLayout.CENTER);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(empty);
        } else {
            for (Booking b : all) list.add(card(b));
        }
        list.revalidate();
        list.repaint();
    }

    private JPanel card(Booking b) {
        Flight f = flights.get(b.flightId);
        JPanel p = new JPanel(new BorderLayout(14, 6));
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0,
                        b.status.equals("CONFIRMED") ? Theme.OK : Theme.BAD),
                Theme.pad(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel left = new JPanel(new GridLayout(0, 1));
        left.setOpaque(false);
        JLabel route = Theme.title((f == null ? b.flightId : f.from + "  →  " + f.to), 17);
        String who = b.passengers.isEmpty() ? "" : String.valueOf(b.passengers.get(0).getOrDefault("name", ""));
        JLabel meta = Theme.muted("PNR " + b.pnr + (who.isEmpty() ? "" : " · " + who) +
                " · Seats " + String.join(", ", b.seats) +
                " · " + (f == null ? "" : f.date + " " + f.departure) +
                " · ₹" + String.format("%,.0f", b.totalPrice), 12);
        left.add(route);
        left.add(meta);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(Theme.pill(b.status, b.status.equals("CONFIRMED") ? new Color(0x166534) : new Color(0x991B1B),
                b.status.equals("CONFIRMED") ? new Color(0xDCFCE7) : new Color(0xFEE2E2)));
        JButton view = Theme.button("View ticket");
        view.addActionListener(e -> {
            if (f != null) new TicketDialog(SwingUtilities.getWindowAncestor(this), b, f).setVisible(true);
            else JOptionPane.showMessageDialog(this, "Flight details unavailable.");
        });
        right.add(view);
        if (b.status.equals("CONFIRMED")) {
            JButton cancel = Theme.danger("Cancel");
            cancel.addActionListener(e -> {
                int ok = JOptionPane.showConfirmDialog(this, "Cancel booking " + b.pnr + "? Seats are released.",
                        "Cancel booking", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
                BookingService.Result r = bookings.cancel(b.pnr, userId, admin);
                if (r.error != null) JOptionPane.showMessageDialog(this, r.error);
                else JOptionPane.showMessageDialog(this, "Booking " + b.pnr + " cancelled. Refund mocked.");
                refresh();
                onChanged.changed();
            });
            right.add(cancel);
        }

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }
}
