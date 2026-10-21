package com.airline.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Shared colors, fonts and small widget factories for the desktop UI. */
public final class Theme {
    public static final Color BG = new Color(0xEEF2F7);
    public static final Color CARD = Color.WHITE;
    public static final Color INK = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color PRIMARY = new Color(0x0EA5E9);
    public static final Color PRIMARY_DARK = new Color(0x0369A1);
    public static final Color OK = new Color(0x16A34A);
    public static final Color BAD = new Color(0xDC2626);
    public static final Color LINE = new Color(0xE2E8F0);
    public static final Color INPUT_BG = new Color(0xF8FAFC);
    public static final Color NAVY = new Color(0x0F172A);
    public static final Color SLATE = new Color(0x1E293B);

    private Theme() {}

    public static Font font(int size) { return new Font("SansSerif", Font.PLAIN, size); }
    public static Font bold(int size) { return new Font("SansSerif", Font.BOLD, size); }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFont(bold(13));
        b.setBackground(CARD);
        b.setForeground(INK);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(8, 14, 8, 14)));
        return b;
    }

    public static JButton primary(String text) {
        JButton b = button(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setBorder(new CompoundBorder(new LineBorder(PRIMARY_DARK), new EmptyBorder(9, 18, 9, 18)));
        return b;
    }

    public static JButton danger(String text) {
        JButton b = button(text);
        b.setForeground(BAD);
        return b;
    }

    /** Visible outline + padding, so inputs never blend into the white card. */
    public static Border inputBorder() {
        return new CompoundBorder(new LineBorder(LINE), new EmptyBorder(8, 10, 8, 10));
    }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(font(14));
        f.setForeground(INK);
        f.setBackground(INPUT_BG);
        f.setCaretColor(INK);
        f.setOpaque(true);
        f.setBorder(inputBorder());
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(font(14));
        f.setForeground(INK);
        f.setBackground(INPUT_BG);
        f.setCaretColor(INK);
        f.setOpaque(true);
        f.setBorder(inputBorder());
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(font(14));
        c.setBackground(CARD);
        c.setForeground(INK);
        c.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(6, 8, 6, 8)));
        return c;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(bold(12));
        l.setForeground(new Color(0x334155));
        return l;
    }

    public static JLabel title(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(bold(size));
        l.setForeground(INK);
        return l;
    }

    public static JLabel muted(String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(font(size));
        l.setForeground(MUTED);
        return l;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setOpaque(true);
        return p;
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    /** Foreground colour for a booking status badge. */
    public static Color statusFg(String status) {
        if (status == null) return MUTED;
        switch (status) {
            case "CONFIRMED": return new Color(0x166534);
            case "PENDING": return new Color(0x92400E);
            default: return new Color(0x991B1B);
        }
    }

    /** Background colour for a booking status badge. */
    public static Color statusBg(String status) {
        if (status == null) return new Color(0xF1F5F9);
        switch (status) {
            case "CONFIRMED": return new Color(0xDCFCE7);
            case "PENDING": return new Color(0xFEF3C7);
            default: return new Color(0xFEE2E2);
        }
    }

    /** Status badge label. */
    public static JLabel statusPill(String status) {
        String text = status == null ? "PENDING" : status;
        if (text.equals("PENDING")) text = "PENDING · awaiting admin";
        return pill(text, statusFg(status), statusBg(status));
    }

    public static Border round() {
        return new LineBorder(LINE);
    }

    /** Small rounded pill used for status/seat counts. */
    public static JLabel pill(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(bold(12));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    /** Applies the app look and feel to a window. */
    public static void style(JFrame f) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        f.setBackground(BG);
    }
}
