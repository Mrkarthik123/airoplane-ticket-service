package com.airline.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A surface that can be raised (card, button) or inset (input, pressed state).
 * The shadow is painted by the component itself, so the component reserves a
 * margin of {@link #MARGIN} pixels around the visible surface.
 */
public class SoftPanel extends JPanel {

    public enum Style { RAISED, INSET, FLAT }

    /** Space reserved around the visible surface so the shadow is not clipped. */
    public static final int MARGIN = 9;

    private final Style style;
    private int radius = 16;
    private int blur = 7;

    public SoftPanel(Style style, LayoutManager layout) {
        super(layout);
        this.style = style;
        setOpaque(false);
    }

    public SoftPanel(Style style) {
        this(style, null);
    }

    public SoftPanel radius(int r) { this.radius = r; return this; }
    public SoftPanel blur(int b) { this.blur = b; return this; }

    @Override
    public Insets getInsets() {
        Insets b = super.getInsets();
        int m = style == Style.FLAT ? 0 : MARGIN;
        return new Insets(b.top + m, b.left + m, b.bottom + m, b.right + m);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(Theme.BASE);
        g2.fillRect(0, 0, w, h);
        g2.dispose();

        if (style == Style.FLAT) {
            super.paintComponent(g);
            return;
        }

        int m = MARGIN;
        int x = m, y = m, ww = w - 2 * m, hh = h - 2 * m;
        if (ww <= 0 || hh <= 0) {
            super.paintComponent(g);
            return;
        }

        Graphics2D s = (Graphics2D) g.create();
        s.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        s.setColor(Theme.BASE);
        s.fillRoundRect(x, y, ww, hh, radius, radius);
        if (style == Style.RAISED) {
            Soft.outerLight(s, x, y, ww, hh, radius, Theme.HIGHLIGHT, blur, 2, 2, 150);
            Soft.outerShadow(s, x, y, ww, hh, radius, Theme.SHADOW, blur, 2, 2, 120);
        } else {
            Soft.innerShadow(s, x, y, ww, hh, radius, Theme.SHADOW, Theme.HIGHLIGHT, blur);
        }
        s.dispose();
        super.paintComponent(g);
    }
}
