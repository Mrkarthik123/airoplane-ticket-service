package com.airline.ui;

import com.airline.model.Flight;
import com.airline.model.User;
import com.airline.service.AuthService;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import com.airline.store.DataStore;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Main desktop window of Airoplane Ticket Service.
 * Tabs: Search · Airports · My bookings · Admin.
 */
public class MainFrame extends JFrame {
    private final DataStore db;
    private final AuthService auth;
    private final FlightService flights;
    private final BookingService bookings;
    private final User user;
    private final String token;
    private Runnable onLogout;

    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel userChip = new JLabel();
    private SearchPanel searchPanel;
    private BookingsPanel bookingsPanel;
    private AdminPanel adminPanel;

    public MainFrame(DataStore db, AuthService auth, FlightService flights, BookingService bookings,
                     User user, String token, String serverUrl) {
        super("Airoplane Ticket Service");
        this.db = db;
        this.auth = auth;
        this.flights = flights;
        this.bookings = bookings;
        this.user = user;
        this.token = token;

        Theme.style(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1120, 740);
        setLocationRelativeTo(null);
        URL icon = MainFrame.class.getResource("/icons/icon-512.png");
        if (icon != null) setIconImage(Toolkit.getDefaultToolkit().getImage(icon));

        setLayout(new BorderLayout());
        add(header(serverUrl), BorderLayout.NORTH);

        searchPanel = new SearchPanel(flights, this::openBooking);
        AirportsPanel airportsPanel = new AirportsPanel(this::pickAirport);
        bookingsPanel = new BookingsPanel(bookings, flights, user.id, user.role.equals("admin"), this::refreshAll);

        tabs.setFont(Theme.bold(13));
        tabs.addTab("🔍 Search", searchPanel);
        tabs.addTab("🗺 Airports", airportsPanel);
        tabs.addTab("🎫 My bookings", bookingsPanel);
        if (user.role.equals("admin")) {
            adminPanel = new AdminPanel(db, flights, bookings, this::refreshAll);
            tabs.addTab("⚙ Admin", adminPanel);
        }
        add(tabs, BorderLayout.CENTER);
        add(statusBar(serverUrl), BorderLayout.SOUTH);

        refreshAll();
        searchPanel.doSearch();
    }

    private JComponent header(String serverUrl) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.NAVY);
        p.setBorder(Theme.pad(12, 18, 12, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel plane = new JLabel("✈");
        plane.setFont(Theme.bold(24));
        plane.setForeground(new Color(0x38BDF8));
        JLabel brand = Theme.title("Airoplane Ticket Service", 19);
        brand.setForeground(Color.WHITE);
        left.add(plane);
        left.add(brand);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        userChip.setFont(Theme.font(13));
        userChip.setForeground(Color.WHITE);
        userChip.setOpaque(true);
        userChip.setBackground(Theme.SLATE);
        userChip.setBorder(Theme.pad(5, 12, 5, 12));
        userChip.setText(user.name + (user.role.equals("admin") ? "  (admin)" : ""));
        JButton logout = Theme.button("Logout");
        logout.addActionListener(e -> {
            auth.logout(token);
            if (onLogout != null) onLogout.run();
            dispose();
        });
        right.add(userChip);
        right.add(logout);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent statusBar(String serverUrl) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE));
        p.add(Theme.muted("Server: " + serverUrl + "  ·  Phone access: same Wi-Fi + this address", 11));
        return p;
    }

    private void pickAirport(String code) {        searchPanel.setFrom(code);
        tabs.setSelectedIndex(0);
        searchPanel.doSearch();
    }

    private void openBooking(Flight flight, int travellers) {
        new BookingDialog(this, flight, travellers, bookings, user.id).setVisible(true);
        refreshAll();
    }

    private void refreshAll() {
        if (bookingsPanel != null) bookingsPanel.refresh();
        if (adminPanel != null) adminPanel.refresh();
    }

    /** Called when the user logs out (set by the launcher to show the login screen again). */
    public void setOnLogout(Runnable r) { this.onLogout = r; }
}
