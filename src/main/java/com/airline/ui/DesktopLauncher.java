package com.airline.ui;

import com.airline.model.User;
import com.airline.service.AuthService;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import com.airline.store.DataStore;
import javax.swing.*;

/** Starts the native desktop UI: login → main window → (logout) → login … */
public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void launch(DataStore db, AuthService auth, FlightService flights,
                               BookingService bookings, String serverUrl) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null, auth);
            login.setVisible(true);
            User user = login.getUser();
            if (user == null) { System.exit(0); return; }
            MainFrame frame = new MainFrame(db, auth, flights, bookings, user, login.getToken(), serverUrl);
            frame.setOnLogout(() -> SwingUtilities.invokeLater(
                    () -> launch(db, auth, flights, bookings, serverUrl)));
            frame.setVisible(true);
        });
    }
}
