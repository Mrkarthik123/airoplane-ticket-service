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

        SoftPanel card = new SoftPanel(SoftPanel.Style.RAISED, new BorderLayout(0, 10));
        card.setBorder(Theme.pad(18, 20, 18, 20));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel pnr = Theme.title("PNR  " + booking.pnr, 24);
        pnr.setForeground(Theme.PRIMARY_DARK);
        head.add(pnr, BorderLayout.NORTH);
        JLabel sub = Theme.muted(flight.airline + " · " + flight.flightNumber + " · " + flight.from + " → " + flight.to +
                " · " + flight.date + " · " + flight.departure + "–" + flight.arrival, 12);
        head.add(sub, BorderLayout.SOUTH);

        boolean confirmed = "CONFIRMED".equals(booking.status);
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setOpaque(true);
        banner.setBackground(Theme.statusBg(booking.status));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.statusFg(booking.status)), Theme.pad(8, 10, 8, 10)));
        String bannerText = confirmed
                ? "Confirmed by the airline — this e-ticket is valid for travel."
                : "PENDING — this booking is waiting for admin confirmation. The e-ticket is issued only after approval.";
        JLabel bannerTextLabel = Theme.label(bannerText);
        bannerTextLabel.setForeground(Theme.statusFg(booking.status));
        banner.add(bannerTextLabel, BorderLayout.CENTER);

        String[] cols = {"Passenger", "Age", "Gender", "Seat"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (int i = 0; i < booking.passengers.size(); i++) {
            java.util.Map<String, Object> p = booking.passengers.get(i);
            String seat = i < booking.seats.size() ? booking.seats.get(i) : "";
            model.addRow(new Object[]{p.getOrDefault("name", ""), p.getOrDefault("age", ""),
                    p.getOrDefault("gender", ""), seat});
        }
        JTable table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(Theme.BASE);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        JLabel total = Theme.title("Total paid: ₹" + String.format("%,.0f", booking.totalPrice), 15);
        total.setForeground(Theme.PRIMARY_DARK);
        JLabel st = Theme.statusPill(booking.status);
        foot.add(total, BorderLayout.WEST);
        foot.add(st, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(banner, BorderLayout.SOUTH);
        card.add(top, BorderLayout.NORTH);
        card.add(sc, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton print = Theme.button(confirmed ? "🖨 Print ticket" : "🖨 Print (after approval)");
        print.setEnabled(confirmed);
        if (!confirmed) print.setToolTipText("Printing unlocks once the admin confirms this booking.");
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
