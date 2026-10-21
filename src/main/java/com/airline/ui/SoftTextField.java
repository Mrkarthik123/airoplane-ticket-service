package com.airline.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Text field with an inset (pressed into the surface) neumorphic look. */
public class SoftTextField extends JTextField {

    private int radius = 12;
    private int blur = 6;

    public SoftTextField(int cols) {
        super(cols);
        setOpaque(false);
        setFont(Theme.font(14));
        setForeground(Theme.INK);
        setCaretColor(Theme.INK);
        setBorder(new EmptyBorder(9, 12, 9, 12));
    }

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
            g2.setColor(Theme.BASE);
            g2.fillRoundRect(m, m, w, h, radius, radius);
            Color shade = isFocusOwner() ? Soft.mix(Theme.SHADOW, Theme.PRIMARY, 0.45) : Theme.SHADOW;
            Soft.innerShadow(g2, m, m, w, h, radius, shade, Theme.HIGHLIGHT, blur);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
