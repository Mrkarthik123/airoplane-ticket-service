package com.airline.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Neumorphic button: a raised surface that sinks into the panel while it is
 * pressed, with the accent colour used for the primary action.
 */
public class SoftButton extends JButton {

    private int radius = 12;
    private int blur = 6;
    private boolean accent = false;
    private Color face = null;

    public SoftButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(9, 16, 9, 16));
        setFont(Theme.bold(13));
        setForeground(Theme.INK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Accent style: coloured surface with a matching shadow. */
    public SoftButton accent(boolean on) {
        this.accent = on;
        this.face = null;
        setForeground(on ? Color.WHITE : Theme.INK);
        repaint();
        return this;
    }

    /** Custom surface colour, used for the taken seats of the seat map. */
    public SoftButton face(Color c) {
        this.face = c;
        repaint();
        return this;
    }

    public SoftButton radius(int r) { this.radius = r; return this; }

    @Override
    public Insets getInsets() {
        Insets b = super.getInsets();
        int m = blur + 2;
        return new Insets(b.top + m, b.left + m, b.bottom + m, b.right + m);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int m = blur + 2;
        int w = getWidth() - 2 * m, h = getHeight() - 2 * m;
        if (w > 0 && h > 0) {
            Color surface = face != null ? face : (accent ? Theme.PRIMARY : Theme.BASE);
            g2.setColor(surface);
            g2.fillRoundRect(m, m, w, h, radius, radius);
            boolean pressed = getModel().isArmed() && getModel().isPressed();
            if (!isEnabled()) {
                Soft.innerShadow(g2, m, m, w, h, radius, Theme.SHADOW, Theme.HIGHLIGHT, blur);
            } else if (pressed) {
                Soft.innerShadow(g2, m, m, w, h, radius,
                        accent ? Theme.PRIMARY_DARK : Theme.SHADOW, Theme.HIGHLIGHT, blur);
            } else {
                Soft.outerLight(g2, m, m, w, h, radius, Theme.HIGHLIGHT, blur, 2, 2, 140);
                Soft.outerShadow(g2, m, m, w, h, radius,
                        accent ? Theme.PRIMARY_DARK : Theme.SHADOW, blur, 2, 2, 110);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
