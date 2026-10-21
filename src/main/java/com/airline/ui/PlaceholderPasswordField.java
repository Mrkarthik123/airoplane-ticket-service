package com.airline.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Password field with a visible grey hint while it is empty. */
public class PlaceholderPasswordField extends JPasswordField {
    private final String hint;

    public PlaceholderPasswordField(String hint, int cols) {
        super(cols);
        this.hint = hint;
        setFont(Theme.font(14));
        setForeground(Theme.INK);
        setBackground(Theme.INPUT_BG);
        setCaretColor(Theme.INK);
        setOpaque(true);
        setEchoChar('•');
        setBorder(new CompoundBorder(new LineBorder(Theme.LINE), new EmptyBorder(8, 10, 8, 10)));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length > 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(isFocusOwner() ? Theme.PRIMARY : Theme.MUTED);
        g2.setFont(Theme.font(14));
        Insets in = getInsets();
        g2.drawString(hint, in.left, getHeight() - in.bottom - 9);
        g2.dispose();
    }
}
