package com.airline.ui;

import com.airline.model.Flight;
import com.airline.service.FlightService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Search tab: from/to/date/travellers + flight result cards. */
public class SearchPanel extends JPanel {
    public interface OnBook { void book(Flight flight, int travellers); }

    private final FlightService flights;
    private final OnBook onBook;
    private final JComboBox<String> fromSel = Theme.combo(new String[0]);
    private final JComboBox<String> toSel = Theme.combo(new String[0]);
    private final JComboBox<String> dateSel = Theme.combo(new String[0]);
    private final JComboBox<Integer> paxSel = Theme.combo(new Integer[]{1, 2, 3, 4, 5, 6});
    private final JPanel results = new JPanel();
    private final JLabel count = Theme.muted("", 12);

    public SearchPanel(FlightService flights, OnBook onBook) {
        this.flights = flights;
        this.onBook = onBook;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(Theme.pad(14, 14, 14, 14));

        // ---- search form ----
        JPanel form = Theme.card(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; form.add(Theme.label("From"), g);
        g.gridx = 1; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.18;
        fromSel.setPreferredSize(new Dimension(150, 38));
        form.add(fromSel, g);
        g.gridx = 2; g.gridy = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JButton swap = Theme.button("⇄");
        swap.setToolTipText("Swap");
        swap.addActionListener(e -> {
            String a = (String) fromSel.getSelectedItem(), b = (String) toSel.getSelectedItem();
            fromSel.setSelectedItem(b);
            toSel.setSelectedItem(a);
        });
        form.add(swap, g);

        g.gridx = 3; g.gridy = 0; form.add(Theme.label("To"), g);
        g.gridx = 4; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.18;
        toSel.setPreferredSize(new Dimension(150, 38));
        form.add(toSel, g);

        g.gridx = 5; g.gridy = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(Theme.label("Date"), g);
        g.gridx = 6; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.24;
        dateSel.setPreferredSize(new Dimension(170, 38));
        form.add(dateSel, g);

        g.gridx = 7; g.gridy = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(Theme.label("Travellers"), g);
        g.gridx = 8; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.1;
        paxSel.setPreferredSize(new Dimension(80, 38));
        form.add(paxSel, g);

        g.gridx = 9; g.gridy = 0; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JButton search = Theme.primary("🔍 Search flights");
        search.addActionListener(e -> doSearch());
        form.add(search, g);

        // ---- results ----
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setOpaque(false);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(Theme.title("Available flights", 17), BorderLayout.WEST);
        head.add(count, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(results);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(form, BorderLayout.NORTH);
        add(head, BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(head, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        remove(head);
        add(center, BorderLayout.CENTER);

        loadAirports();
    }

    private void loadAirports() {
        String[] codes = AirportsPanel.DATA.keySet().toArray(new String[0]);
        fromSel.setModel(new DefaultComboBoxModel<>(codes));
        toSel.setModel(new DefaultComboBoxModel<>(codes));
        fromSel.setSelectedItem("DEL");
        toSel.setSelectedItem("BOM");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
        String[] dates = new String[14];
        for (int i = 0; i < dates.length; i++) dates[i] = LocalDate.now().plusDays(i + 1).format(fmt);
        dateSel.setModel(new DefaultComboBoxModel<>(dates));
    }

    /** Selects a departure airport (used by the Airports tab). */
    public void setFrom(String code) {
        fromSel.setSelectedItem(code);
    }

    public void doSearch() {
        String from = (String) fromSel.getSelectedItem();
        String to = (String) toSel.getSelectedItem();
        String dateText = (String) dateSel.getSelectedItem();
        if (from == null || to == null || dateText == null) return;
        if (from.equals(to)) { JOptionPane.showMessageDialog(this, "From and To cannot be the same."); return; }
        String date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")).toString();

        List<Flight> list = flights.search(from, to, date);
        results.removeAll();
        count.setText(list.size() + " flight" + (list.size() == 1 ? "" : "s") + " found");
        if (list.isEmpty()) {
            JPanel empty = Theme.card(new BorderLayout());
            empty.add(Theme.muted("No flights on this route and date. Try another day or city.", 13), BorderLayout.CENTER);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            results.add(empty);
        } else {
            for (Flight f : list) results.add(flightCard(f));
        }
        results.revalidate();
        results.repaint();
    }

    private JPanel flightCard(Flight f) {
        JPanel p = new JPanel(new BorderLayout(14, 6));
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, Theme.PRIMARY),
                Theme.pad(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        JPanel left = new JPanel(new GridLayout(0, 1));
        left.setOpaque(false);
        JLabel route = Theme.title(f.from + "  →  " + f.to, 18);
        JLabel meta = Theme.muted(f.airline + " · " + f.flightNumber + " · " + f.date + " · " +
                f.departure + "–" + f.arrival + " · " + (f.durationMin / 60) + "h " + (f.durationMin % 60) + "m", 12);
        left.add(route);
        left.add(meta);

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 6, 2, 6);
        g.anchor = GridBagConstraints.EAST;

        g.gridx = 0; g.gridy = 0;
        right.add(Theme.pill(f.availableSeats() + " seats left",
                f.availableSeats() > 20 ? new Color(0x166534) : new Color(0x991B1B),
                f.availableSeats() > 20 ? new Color(0xDCFCE7) : new Color(0xFEE2E2)), g);

        g.gridx = 1; g.gridy = 0;
        JPanel fare = new JPanel(new GridLayout(0, 1));
        fare.setOpaque(false);
        JLabel price = Theme.title("₹" + String.format("%,.0f", f.price), 18);
        price.setForeground(Theme.PRIMARY_DARK);
        price.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel per = Theme.muted("per adult", 11);
        per.setHorizontalAlignment(SwingConstants.RIGHT);
        fare.add(price);
        fare.add(per);
        right.add(fare, g);

        g.gridx = 2; g.gridy = 0;
        JButton book = Theme.primary("Book now");
        book.addActionListener(e -> onBook.book(f, (Integer) paxSel.getSelectedItem()));
        right.add(book, g);

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }
}
