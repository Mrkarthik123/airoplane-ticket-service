package com.airline;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

/**
 * Small native window shown by the macOS/Windows desktop app (jpackage bundle).
 * Gives the user a visible launcher: status, "Open in browser" and "Quit".
 */
public class DesktopApp {
    private static final Color NAVY = new Color(0x0f172a);
    private static final Color SKY = new Color(0x0ea5e9);

    public static void show(String url, Runnable onQuit) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Airoplane Ticket Service");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setSize(460, 300);
            frame.setLocationByPlatform(true);
            java.net.URL iconUrl = DesktopApp.class.getResource("/icons/icon-512.png");
            if (iconUrl != null) frame.setIconImage(Toolkit.getDefaultToolkit().getImage(iconUrl));

            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.setBackground(NAVY);

            JLabel icon = new JLabel("✈", SwingConstants.CENTER);
            icon.setFont(new Font("SansSerif", Font.PLAIN, 54));
            icon.setForeground(Color.WHITE);
            icon.setBorder(BorderFactory.createEmptyBorder(26, 0, 6, 0));

            JLabel title = new JLabel("Airoplane Ticket Service", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 19));
            title.setForeground(Color.WHITE);

            JLabel status = new JLabel("<html><center>Running at<br><b>" + url + "</b></center></html>", SwingConstants.CENTER);
            status.setFont(new Font("SansSerif", Font.PLAIN, 13));
            status.setForeground(new Color(0xbae6fd));
            status.setBorder(BorderFactory.createEmptyBorder(12, 0, 18, 0));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttons.setOpaque(false);
            JButton open = new JButton("Open in Browser");
            JButton quit = new JButton("Stop & Quit");
            style(open, SKY, Color.WHITE);
            style(quit, new Color(0x1e293b), new Color(0xcbd5e1));
            open.addActionListener(e -> browse(url));
            quit.addActionListener(e -> {
                onQuit.run();
                System.exit(0);
            });

            buttons.add(open);
            buttons.add(quit);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(icon, BorderLayout.NORTH);
            top.add(title, BorderLayout.CENTER);

            root.add(top, BorderLayout.NORTH);
            root.add(status, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);
            root.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));

            frame.setContentPane(root);
            frame.getRootPane().setDefaultButton(open);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void style(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(150, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void browse(String url) {
        try {
            Desktop d = Desktop.getDesktop();
            if (d.isSupported(Desktop.Action.BROWSE)) d.browse(new URI(url));
            else new ProcessBuilder("open", url).start();
        } catch (Exception ignored) {}
    }
}
