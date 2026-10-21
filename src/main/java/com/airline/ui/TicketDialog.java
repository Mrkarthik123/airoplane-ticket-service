package com.airline.ui;

import com.airline.model.Booking;
import com.airline.model.Flight;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;

/** E-ticket window with PNR, passenger table and print support. */
public class TicketDialog extends JDialog {
    public TicketDialog(Window owner, Booking booking, Flight flight) {
        super(owner, "E-Ticket", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Theme.BG);
        setSize(620, 540);
        setLocationRelativeTo(owner);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(new Color(0xF0F9FF));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 2, true), Theme.pad(16, 16, 16, 16)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel pnr = Theme.title("PNR  " + booking.pnr, 24);
        pnr.setForeground(Theme.PRIMARY_DARK);
        head.add(pnr, BorderLayout.NORTH);
        JLabel sub = Theme.muted(flight.airline + " · " + flight.flightNumber + " · " + flight.from + " → " + flight.to +
                " · " + flight.date + " · " + flight.departure + "–" + flight.arrival, 12);
        head.add(sub, BorderLayout.SOUTH);

        String[] cols = {"Passenger", "Age", "Gender", "Seat"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (int i = 0; i < booking.passengers.size(); i++) {
            java.util.Map<String, Object> p = booking.passengers.get(i);
            String seat = i < booking.seats.size() ? booking.seats.get(i) : "";
            model.addRow(new Object[]{p.getOrDefault("name", ""), p.getOrDefault("age", ""),
                    p.getOrDefault("gender", ""), seat});
        }
        JTable table = new JTable(model);
        table.setFont(Theme.font(13));
        table.setRowHeight(24);
        table.setBackground(Color.WHITE);
        table.setGridColor(Theme.LINE);
        table.setShowGrid(true);
        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(Theme.LINE));

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        JLabel total = Theme.title("Total paid: ₹" + String.format("%,.0f", booking.totalPrice), 15);
        total.setForeground(Theme.PRIMARY_DARK);
        JLabel st = Theme.pill(booking.status, booking.status.equals("CONFIRMED")
                ? new Color(0x166534) : new Color(0x991B1B),
                booking.status.equals("CONFIRMED") ? new Color(0xDCFCE7) : new Color(0xFEE2E2));
        foot.add(total, BorderLayout.WEST);
        foot.add(st, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);
        card.add(sc, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton print = Theme.button("🖨 Print");
        print.addActionListener(e -> printTicket(booking, flight));
        JButton close = Theme.primary("Close");
        close.addActionListener(e -> dispose());
        buttons.add(print);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);
    }

    private void printTicket(Booking b, Flight f) {
        JTextArea area = new JTextArea();
        area.setFont(Theme.font(12));
        area.setText("AIROPLANE TICKET SERVICE\n" +
                "--------------------------------------\n" +
                "PNR: " + b.pnr + "        Status: " + b.status + "\n" +
                "Flight: " + f.airline + " " + f.flightNumber + "\n" +
                "Route: " + f.from + " -> " + f.to + "\n" +
                "Date: " + f.date + "   " + f.departure + " - " + f.arrival + "\n" +
                "Payment: " + b.paymentId + "\n" +
                "--------------------------------------\n");
        for (int i = 0; i < b.passengers.size(); i++) {
            java.util.Map<String, Object> p = b.passengers.get(i);
            area.append(String.format("%-20s %-4s %-7s Seat %s%n", p.getOrDefault("name", ""),
                    p.getOrDefault("age", ""), p.getOrDefault("gender", ""),
                    i < b.seats.size() ? b.seats.get(i) : ""));
        }
        area.append("--------------------------------------\nTotal: Rs. " + String.format("%,.0f", b.totalPrice) + "\n");
        try {
            area.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Could not print: " + ex.getMessage());
        }
    }
}
