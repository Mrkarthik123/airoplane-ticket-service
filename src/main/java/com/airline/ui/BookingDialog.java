package com.airline.ui;

import com.airline.model.Booking;
import com.airline.model.Flight;
import com.airline.service.AuthService;
import com.airline.service.BookingService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Booking flow: seat map, passenger details, mock payment. */
public class BookingDialog extends JDialog {
    private final Flight flight;
    private final int travellers;
    private final BookingService bookings;
    private final String userId;

    private final JPanel seatPanel = new JPanel();
    private final List<String> selectedSeats = new ArrayList<>();
    private final Map<Integer, JTextField> nameFields = new LinkedHashMap<>();
    private final Map<Integer, JTextField> ageFields = new LinkedHashMap<>();
    private final Map<Integer, JComboBox<String>> genderFields = new LinkedHashMap<>();
    private final JTextField cardField = Theme.field(18);
    private final JLabel totalLabel = Theme.title("", 18);
    private final JLabel seatInfo = Theme.muted("", 12);

    public BookingDialog(Window owner, Flight flight, int travellers, BookingService bookings, String userId) {
        super(owner, "Complete booking — " + flight.airline + " " + flight.flightNumber, ModalityType.APPLICATION_MODAL);
        this.flight = flight;
        this.travellers = travellers;
        this.bookings = bookings;
        this.userId = userId;

        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(Theme.BG);
        setSize(920, 620);
        setLocationRelativeTo(owner);

        add(header(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 14, 0));
        center.setOpaque(false);
        center.add(seatsCard());
        center.add(detailsCard());
        add(center, BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);

        renderSeats();
        updateTotal();
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.NAVY);
        p.setBorder(Theme.pad(14, 18, 14, 18));
        JLabel t = Theme.title(flight.from + "  →  " + flight.to, 20);
        t.setForeground(Color.WHITE);
        JLabel m = Theme.muted(flight.airline + " · " + flight.flightNumber + " · " + flight.date + " · " +
                flight.departure + "–" + flight.arrival, 12);
        m.setForeground(new Color(0xBAE6FD));
        p.add(t, BorderLayout.NORTH);
        p.add(m, BorderLayout.SOUTH);
        return p;
    }

    private JPanel seatsCard() {
        JPanel card = Theme.card(new BorderLayout(0, 8));
        JLabel h = Theme.title("1. Select seats (" + travellers + (travellers == 1 ? " person" : " people") + ")", 15);
        card.add(h, BorderLayout.NORTH);

        seatPanel.setLayout(new GridLayout(30, 7, 4, 4));
        seatPanel.setBackground(new Color(0xF8FAFC));
        seatPanel.setBorder(Theme.pad(8, 8, 8, 8));
        JScrollPane sc = new JScrollPane(seatPanel);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(new Color(0xF8FAFC));
        sc.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sc, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(seatLegend("Free", Color.WHITE, Theme.LINE));
        legend.add(seatLegend("Taken", new Color(0xE2E8F0), new Color(0xCBD5E1)));
        legend.add(seatLegend("Selected", Theme.PRIMARY, Theme.PRIMARY));
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    private JComponent seatLegend(String text, Color bg, Color border) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        JButton sq = new JButton();
        sq.setPreferredSize(new Dimension(20, 20));
        sq.setBackground(bg);
        sq.setBorder(BorderFactory.createLineBorder(border));
        sq.setEnabled(false);
        row.add(sq);
        row.add(Theme.muted(text, 11));
        return row;
    }

    private JPanel detailsCard() {
        JPanel card = Theme.card(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 4;
        card.add(Theme.title("2. Passenger details (" + travellers + (travellers == 1 ? " person" : " people") + ")", 15), g);
        g.gridy = 1; g.gridwidth = 4; g.weightx = 1;
        card.add(buildPassengerRows(), g);

        g.gridy = 2; g.gridwidth = 4;
        card.add(Theme.title("3. Payment (mock)", 15), g);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 1; g.weightx = 0.2;
        card.add(Theme.label("Card number"), g);
        g.gridx = 1; g.gridy = 3; g.gridwidth = 3; g.weightx = 0.8;
        card.setBackground(Theme.CARD);
        cardField.setText("4111 1111 1111 1111");
        card.add(cardField, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 4; g.weightx = 0;
        totalLabel.setForeground(Theme.PRIMARY_DARK);
        card.add(totalLabel, g);
        g.gridy = 5;
        seatInfo.setText("Pick " + travellers + " seat(s) for " + travellers + (travellers == 1 ? " person" : " people") + " and fill each person's name and age.");
        card.add(seatInfo, g);
        g.gridy = 6;
        card.add(Theme.muted("Test card only — no real money is charged.", 11), g);
        return card;
    }

    private JPanel footer() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        JButton cancel = Theme.button("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton pay = Theme.primary("Pay & confirm booking");
        pay.addActionListener(e -> confirm(pay));
        p.add(cancel);
        p.add(pay);
        return p;
    }

    private void renderSeats() {
        seatPanel.removeAll();
        for (int row = 1; row <= 30; row++) {
            for (String col : new String[]{"A", "B", "C", "", "D", "E", "F"}) {
                if (col.isEmpty()) {
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
                b.setMargin(new Insets(1, 1, 1, 1));
                b.setFocusPainted(false);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.setBorder(BorderFactory.createLineBorder(taken ? new Color(0xCBD5E1) : Theme.LINE));
                b.setBackground(taken ? new Color(0xE2E8F0) : Color.WHITE);
                b.setForeground(taken ? new Color(0x94A3B8) : Theme.INK);
                b.setEnabled(!taken);
                b.addActionListener(e -> {
                    if (selectedSeats.contains(seat)) {
                        selectedSeats.remove(seat);
                        b.setBackground(Color.WHITE);
                        b.setForeground(Theme.INK);
                        b.setBorder(BorderFactory.createLineBorder(Theme.LINE));
                    } else {
                        if (selectedSeats.size() >= travellers) {
                            JOptionPane.showMessageDialog(this, "You can pick " + travellers + " seat(s) only.");
                            return;
                        }
                        selectedSeats.add(seat);
                        b.setBackground(Theme.PRIMARY);
                        b.setForeground(Color.WHITE);
                        b.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY));
                    }
                    updateTotal();
                });
                seatPanel.add(b);
            }
        }
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private JPanel buildPassengerRows() {
        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // column headers so every box is labelled
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        rows.add(Theme.muted("#", 11), g);
        g.gridx = 1; g.weightx = 1;
        rows.add(Theme.label("Full name"), g);
        g.gridx = 2; g.weightx = 0;
        rows.add(Theme.label("Age"), g);
        g.gridx = 3; g.weightx = 0;
        rows.add(Theme.label("Gender"), g);

        for (int i = 0; i < travellers; i++) {
            int y = i + 1;
            g.gridx = 0; g.gridy = y; g.weightx = 0;
            rows.add(Theme.label("P" + (i + 1)), g);

            JTextField name = new PlaceholderField("Passenger " + (i + 1) + " name", 16);
            g.gridx = 1; g.weightx = 1;
            rows.add(name, g);

            JTextField age = new PlaceholderField("Age", 4);
            age.setHorizontalAlignment(SwingConstants.CENTER);
            g.gridx = 2; g.weightx = 0;
            rows.add(age, g);

            JComboBox<String> gender = Theme.combo(new String[]{"Male", "Female", "Other"});
            g.gridx = 3; g.weightx = 0;
            rows.add(gender, g);

            nameFields.put(i, name);
            ageFields.put(i, age);
            genderFields.put(i, gender);
        }

        g.gridx = 0; g.gridy = travellers + 1; g.gridwidth = 4; g.weightx = 1;
        rows.add(Theme.muted("Number of people in this booking: " + travellers +
                (travellers == 1 ? " person" : " people"), 11), g);
        return rows;
    }

    private void updateTotal() {
        double total = flight.price * travellers;
        totalLabel.setText("Total: ₹" + String.format("%,.0f", total) + "  (" + travellers +
                (travellers == 1 ? " person × " : " people × ") + "₹" + String.format("%,.0f", flight.price) + ")");
        if (selectedSeats.size() == travellers) {
            seatInfo.setText("Seats selected: " + String.join(", ", selectedSeats));
            seatInfo.setForeground(Theme.OK);
        } else {
            seatInfo.setText("Pick " + (travellers - selectedSeats.size()) + " more seat(s) for " + travellers + (travellers == 1 ? " person" : " people") + ".");
            seatInfo.setForeground(Theme.MUTED);
        }
    }

    private void confirm(JButton pay) {
        if (selectedSeats.size() != travellers) {
            JOptionPane.showMessageDialog(this, "Please select " + travellers + " seat(s).");
            return;
        }
        List<Map<String, Object>> pax = new ArrayList<>();
        for (int i = 0; i < travellers; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter passenger name for P" + (i + 1) + ".");
                return;
            }
            String ageText = ageFields.get(i).getText().trim();
            if (ageText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter age for P" + (i + 1) + ".");
                ageFields.get(i).requestFocusInWindow();
                return;
            }
            int age;
            try { age = Integer.parseInt(ageText); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age for P" + (i + 1) + " must be a number.");
                ageFields.get(i).requestFocusInWindow();
                return;
            }
            if (age < 1 || age > 120) {
                JOptionPane.showMessageDialog(this, "Age for P" + (i + 1) + " must be between 1 and 120.");
                ageFields.get(i).requestFocusInWindow();
                return;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", name);
            m.put("age", ageText);
            m.put("gender", (String) genderFields.get(i).getSelectedItem());
            pax.add(m);
        }
        String card = cardField.getText().replaceAll("\\s", "");
        if (card.length() < 12) {
            JOptionPane.showMessageDialog(this, "Enter a valid test card number (12+ digits).");
            return;
        }
        double total = flight.price * travellers;
        Map<String, Object> payment = bookings.mockPay(total, card.substring(card.length() - 4));
        BookingService.Result r = bookings.create(userId, flight.id, pax, new ArrayList<>(selectedSeats),
                String.valueOf(payment.get("paymentId")), total);
        if (r.error != null) {
            JOptionPane.showMessageDialog(this, r.error);
            renderSeats();
            return;
        }
        dispose();
        new TicketDialog(getOwner(), r.booking, flight).setVisible(true);
    }
}
