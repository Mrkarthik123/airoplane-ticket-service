package com.airline.ui;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Neumorphic painting primitives.
 *
 * Swing has no box-shadow, so the soft dual shadow is drawn by stroking the
 * outline of a rounded rectangle several times with a growing radius and a
 * falling alpha, which reads as a blurred shadow.
 */
final class Soft {

    private Soft() {}

    /** A colour with the given alpha. */
    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Mixes two colours, t = 0 gives a, t = 1 gives b. */
    static Color mix(Color a, Color b, double t) {
        double u = 1 - t;
        return new Color((int) Math.round(a.getRed() * u + b.getRed() * t),
                (int) Math.round(a.getGreen() * u + b.getGreen() * t),
                (int) Math.round(a.getBlue() * u + b.getBlue() * t));
    }

    private static Shape rounded(int x, int y, int w, int h, int arc) {
        return new RoundRectangle2D.Float(x, y, w, h, arc, arc);
    }

    /**
     * Draws a blurred shadow outside the given rounded rectangle.
     * The shadow is offset down and to the right, as if lit from the top left.
     */
    static void outerShadow(Graphics2D g2, int x, int y, int w, int h, int arc,
                             Color shadow, int blur, int dx, int dy, int strength) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2f));
        for (int k = blur; k >= 1; k--) {
            double f = k / (double) blur;
            int a = (int) Math.round(strength * Math.pow(f, 1.6));
            g.setColor(alpha(shadow, a));
            g.draw(rounded(x - k + dx, y - k + dy, w + 2 * k, h + 2 * k, arc + k));
        }
        g.dispose();
    }

    /**
     * Draws a blurred light rim outside the given rounded rectangle, offset up
     * and to the left. Together with {@link #outerShadow} it produces the
     * raised look of a light source in the top left corner.
     */
    static void outerLight(Graphics2D g2, int x, int y, int w, int h, int arc,
                            Color highlight, int blur, int dx, int dy, int strength) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2f));
        for (int k = blur; k >= 1; k--) {
            double f = k / (double) blur;
            int a = (int) Math.round(strength * Math.pow(f, 1.6));
            g.setColor(alpha(highlight, a));
            g.draw(rounded(x - k - dx, y - k - dy, w + 2 * k, h + 2 * k, arc + k));
        }
        g.dispose();
    }

    /**
     * Draws the inset (pressed) look: a soft shadow along the top left inside
     * the shape and a soft highlight along the bottom right. The two gradients
     * run diagonally, which is how the light of a neumorphic surface falls.
     */
    static void innerShadow(Graphics2D g2, int x, int y, int w, int h, int arc,
                             Color shadow, Color highlight, int blur) {
        if (w <= 0 || h <= 0) return;
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape shape = rounded(x, y, w, h, arc);
        g.setClip(shape);

        float ex = Math.min(w, h) * 0.6f + blur;
        float exX = Math.min(ex, w);
        float exY = Math.min(ex, h);

        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(x, y),
                new Point2D.Float(x + exX, y + exY),
                new float[]{0f, 1f},
                new Color[]{alpha(shadow, 130), alpha(shadow, 0)}));
        g.fill(shape);

        g.setPaint(new LinearGradientPaint(
                new Point2D.Float(x + w, y + h),
                new Point2D.Float(x + w - exX, y + h - exY),
                new float[]{0f, 1f},
                new Color[]{alpha(highlight, 190), alpha(highlight, 0)}));
        g.fill(shape);
        g.dispose();
    }
}
