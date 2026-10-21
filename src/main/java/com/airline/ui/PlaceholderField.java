package com.airline.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Text field with a visible grey hint that disappears as soon as the user types. */
public class PlaceholderField extends SoftTextField {
    private final String hint;

    public PlaceholderField(String hint, int cols) {
        super(cols);
        this.hint = hint;
        setFont(Theme.font(14));
        setForeground(Theme.INK);
        setCaretColor(Theme.INK);
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    public String getHint() { return hint; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!getText().isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(isFocusOwner() ? Theme.PRIMARY : Theme.MUTED);
        g2.setFont(Theme.font(14));
        Insets in = getInsets();
        g2.drawString(hint, in.left, getHeight() - in.bottom - 9);
        g2.dispose();
    }
}
