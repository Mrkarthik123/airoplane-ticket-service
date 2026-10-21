package com.airline.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Neumorphic theme: one soft base colour, a light rim and a soft shadow, and
 * very few borders. Every widget is built on top of the soft components so the
 * whole desktop application shares the same surface language.
 */
public final class Theme {

    /** The single surface colour everything is drawn on. */
    public static final Color BASE = new Color(0xE6EAF2);
    /** Light rim, top left. */
    public static final Color HIGHLIGHT = new Color(0xFFFFFF);
    /** Soft shadow, bottom right. */
    public static final Color SHADOW = new Color(0xB7C0D1);

    public static final Color INK = new Color(0x39445A);
    public static final Color MUTED = new Color(0x7A869B);
    public static final Color PRIMARY = new Color(0x5B7DFA);
    public static final Color PRIMARY_DARK = new Color(0x3B5BD6);
    public static final Color OK = new Color(0x2FA36B);
    public static final Color BAD = new Color(0xE0576A);
    public static final Color WARN = new Color(0xD99129);

    /** Cards and the window background share the base colour. */
    public static final Color BG = BASE;
    public static final Color CARD = BASE;
    public static final Color INPUT_BG = BASE;
    public static final Color LINE = new Color(0xC9D1E0);
    public static final Color NAVY = BASE;
    public static final Color SLATE = new Color(0xD3DAE7);

    private Theme() {}

    public static Font font(int size) { return new Font("SansSerif", Font.PLAIN, size); }
    public static Font bold(int size) { return new Font("SansSerif", Font.BOLD, size); }

    /* ------------------------------------------------------------------ buttons */

    public static SoftButton button(String text) {
        return new SoftButton(text);
    }

    public static SoftButton primary(String text) {
        return new SoftButton(text).accent(true);
    }

    public static SoftButton danger(String text) {
        SoftButton b = new SoftButton(text);
        b.setForeground(BAD);
        return b;
    }

    /* ------------------------------------------------------------------- inputs */

    public static SoftTextField field(int cols) {
        return new SoftTextField(cols);
    }

    public static SoftPasswordField passwordField(int cols) {
        return new SoftPasswordField(cols);
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(font(14));
        c.setForeground(INK);
        c.setOpaque(false);
        c.setBorder(new EmptyBorder(9, 12, 9, 12));
        c.setUI(new SoftComboUI());
        c.setBackground(BASE);
        return c;
    }

    /** A row of controls that sits in an inset well. */
    public static <T> JComboBox<T> comboInset(T[] items) {
        JComboBox<T> c = combo(items);
        return c;
    }

    /* ------------------------------------------------------------------- labels */

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(bold(12));
        l.setForeground(INK);
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

    /* ------------------------------------------------------------------ surfaces */

    /** A raised card. */
    public static SoftPanel card(LayoutManager layout) {
        SoftPanel p = new SoftPanel(SoftPanel.Style.RAISED, layout);
        p.setBorder(pad(16, 16, 16, 16));
        return p;
    }

    /** A sunken well, used for input groups and tables. */
    public static SoftPanel inset(LayoutManager layout) {
        SoftPanel p = new SoftPanel(SoftPanel.Style.INSET, layout);
        p.setBorder(pad(12, 12, 12, 12));
        return p;
    }

    /** A plain surface with no shadow. */
    public static JPanel plain(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BASE);
        p.setOpaque(true);
        return p;
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    /* -------------------------------------------------------------------- badges */

    /** Foreground colour for a booking status badge. */
    public static Color statusFg(String status) {
        if (status == null) return MUTED;
        switch (status) {
            case "CONFIRMED": return new Color(0x1F7A4D);
            case "PENDING": return new Color(0xA9651A);
            default: return new Color(0xA32F3D);
        }
    }

    /** Background colour for a booking status badge. */
    public static Color statusBg(String status) {
        if (status == null) return new Color(0xDDE3EC);
        switch (status) {
            case "CONFIRMED": return new Color(0xD3EEDF);
            case "PENDING": return new Color(0xF5E4C8);
            default: return new Color(0xF6D8DC);
        }
    }

    /** Status badge label. */
    public static JLabel statusPill(String status) {
        String text = status == null ? "PENDING" : status;
        if (text.equals("PENDING")) text = "PENDING · awaiting admin";
        return pill(text, statusFg(status), statusBg(status));
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

    public static Border round() {
        return new EmptyBorder(0, 0, 0, 0);
    }

    /* -------------------------------------------------------------------- tables */

    /** Applies the soft look to a table. */
    public static void styleTable(JTable t) {
        t.setBackground(BASE);
        t.setForeground(INK);
        t.setGridColor(new Color(0xD6DCE8));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 6));
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(0xD5DEFF));
        t.setSelectionForeground(INK);
        t.setFont(font(13));
        t.setBorder(new EmptyBorder(0, 0, 0, 0));

        JTableHeader head = t.getTableHeader();
        head.setBackground(BASE);
        head.setForeground(INK);
        head.setFont(bold(12));
        head.setReorderingAllowed(false);
        head.setBorder(new EmptyBorder(4, 0, 4, 0));
        head.setPreferredSize(new Dimension(0, 30));

        TableCellRenderer renderer = t.getColumnCount() > 0 ? t.getCellRenderer(0, 0) : null;
        if (renderer != null) {
            t.setDefaultRenderer(Object.class, new SoftCellRenderer(renderer));
        }
    }

    /** Colours a status column with the soft status palette. */
    public static void statusColumn(JTable t, int column) {
        if (t == null || column >= t.getColumnCount()) return;
        t.getColumnModel().getColumn(column).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                           boolean focused, int row, int columnIndex) {
                String status = value == null ? "" : value.toString();
                JLabel l = new JLabel(status);
                l.setFont(bold(12));
                l.setForeground(statusFg(status));
                l.setOpaque(true);
                l.setBackground(selected ? new Color(0xD5DEFF) : BASE);
                l.setBorder(new EmptyBorder(2, 8, 2, 8));
                return l;
            }
        });
    }

    /** Renders cells without the default opaque selection blocks. */
    private static class SoftCellRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        SoftCellRenderer(TableCellRenderer delegate) { this.delegate = delegate; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            Component c = delegate.getTableCellRendererComponent(table, value, selected, false, row, column);
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setBackground(selected ? new Color(0xD5DEFF) : BASE);
                l.setForeground(selected ? INK : (value == null ? MUTED : INK));
                l.setFont(font(13));
                l.setBorder(new EmptyBorder(2, 8, 2, 8));
            }
            return c;
        }
    }

    /* ------------------------------------------------------------------ scrollbars */

    private static void styleScrollBar() {
        UIManager.put("ScrollBar.background", BASE);
        UIManager.put("ScrollBar.foreground", MUTED);
        UIManager.put("ScrollBar.thumb", new Color(0xC3CCDC));
        UIManager.put("ScrollBar.thumbShadow", new Color(0xC3CCDC));
        UIManager.put("ScrollBar.thumbHighlight", new Color(0xE9EDF5));
        UIManager.put("ScrollBar.track", BASE);
        UIManager.put("ScrollBar.width", 12);
    }

    /* -------------------------------------------------------------------- look and feel */

    private static boolean installed;

    /** Installs the look and feel and the soft defaults. Safe to call repeatedly. */
    public static synchronized void install() {
        if (installed) return;
        installed = true;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BASE);
        UIManager.put("OptionPane.background", BASE);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("Button.background", BASE);
        UIManager.put("Label.foreground", INK);
        UIManager.put("TextField.background", BASE);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.caretForeground", INK);
        UIManager.put("TextField.selectionBackground", new Color(0xC9D6FF));
        UIManager.put("TextField.selectionForeground", INK);
        UIManager.put("PasswordField.background", BASE);
        UIManager.put("PasswordField.foreground", INK);
        UIManager.put("ComboBox.background", BASE);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("ComboBox.selectionBackground", new Color(0xC9D6FF));
        UIManager.put("List.background", BASE);
        UIManager.put("List.foreground", INK);
        UIManager.put("Table.background", BASE);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.gridColor", new Color(0xD6DCE8));
        UIManager.put("TableHeader.background", BASE);
        UIManager.put("TableHeader.foreground", INK);
        UIManager.put("TabbedPane.background", BASE);
        UIManager.put("TabbedPane.foreground", INK);
        UIManager.put("TabbedPane.selected", BASE);
        UIManager.put("TabbedPane.selectHighlight", BASE);
        UIManager.put("TabbedPane.contentAreaColor", BASE);
        UIManager.put("TabbedPane.tabAreaBackground", BASE);
        UIManager.put("TabbedPane.tabInsets", new Insets(6, 16, 14, 16));
        UIManager.put("TabbedPane.toplevelBackground", BASE);
        styleScrollBar();
    }

    static {
        install();
    }

    /** Applies the app look and feel to a window. */
    public static void style(JFrame f) {
        install();
        f.setBackground(BASE);
    }

    /** Creates a tabbed pane that uses the soft tabs. */
    public static JTabbedPane tabs() {
        JTabbedPane t = new JTabbedPane();
        t.setUI(new SoftTabbedPaneUI());
        t.setBackground(BASE);
        t.setForeground(INK);
        t.setBorder(new EmptyBorder(0, 0, 0, 0));
        t.setOpaque(false);
        return t;
    }
}
