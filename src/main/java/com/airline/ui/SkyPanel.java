package com.airline.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Animated sky background: gradient sky, drifting clouds and a plane flying across.
 * Used as the window backdrop of the desktop app.
 */
public class SkyPanel extends JPanel {
    private final List<Cloud> clouds = new ArrayList<>();
    private double planeX = -0.15;
    private final Timer timer;

    private static class Cloud {
        double x, y, w, h, speed, alpha;
        Cloud(double x, double y, double w, double h, double speed, double alpha) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.speed = speed; this.alpha = alpha;
        }
    }

    public SkyPanel() {
        setOpaque(true);
        clouds.add(new Cloud(0.10, 0.62, 0.20, 0.055, 0.0045, 0.85));
        clouds.add(new Cloud(0.45, 0.30, 0.14, 0.040, 0.0028, 0.60));
        clouds.add(new Cloud(0.75, 0.72, 0.26, 0.070, 0.0060, 0.45));
        timer = new Timer(33, e -> {
            planeX += 0.0022;
            if (planeX > 1.25) planeX = -0.25;
            for (Cloud c : clouds) {
                c.x += c.speed;
                if (c.x > 1.2) c.x = -c.w - 0.1;
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setPaint(new GradientPaint(0, 0, new Color(0xBFD4F5), 0, h, new Color(0xEDF2FB)));
        g2.fillRect(0, 0, w, h);

        // sun
        g2.setPaint(new Color(0xFF, 0xF6, 0xD8, 200));
        g2.fillOval((int) (w * 0.82), (int) (h * 0.08), (int) (h * 0.16), (int) (h * 0.16));
        g2.setPaint(new Color(0xFF, 0xF6, 0xD8, 60));
        g2.fillOval((int) (w * 0.82) - 30, (int) (h * 0.08) - 30, (int) (h * 0.16) + 60, (int) (h * 0.16) + 60);

        // clouds
        for (Cloud c : clouds) drawCloud(g2, c, w, h);

        // flying plane
        drawPlane(g2, w, h);
        g2.dispose();
    }

    private void drawCloud(Graphics2D g, Cloud c, int w, int h) {
        double cw = c.w * w, ch = c.h * h, cx = c.x * w, cy = c.y * h;
        g.setPaint(new Color(255, 255, 255, (int) (255 * c.alpha)));
        g.fillOval((int) cx, (int) cy, (int) cw, (int) ch);
        g.fillOval((int) (cx + cw * 0.18), (int) (cy - ch * 0.55), (int) (cw * 0.5), (int) (ch * 1.1));
        g.fillOval((int) (cx + cw * 0.55), (int) (cy - ch * 0.30), (int) (cw * 0.42), (int) (ch * 0.95));
    }

    private void drawPlane(Graphics2D g, int w, int h) {
        double px = planeX * w, py = h * 0.30 + Math.sin(planeX * 6) * h * 0.045;
        double size = Math.max(26.0, h * 0.055);
        g.setPaint(new Color(0x6B, 0x7A, 0x95, 45));
        g.fillOval((int) (px - size * 0.35), (int) (py + size * 0.55), (int) (size * 1.5), (int) (size * 0.22));

        AffineTransform old = g.getTransform();
        g.translate(px, py);
        g.rotate(-0.07);
        Path2D plane = new Path2D.Double();
        plane.moveTo(-size, 0);
        plane.lineTo(size * 0.15, -size * 0.16);
        plane.lineTo(size * 0.9, 0);
        plane.lineTo(size * 0.15, size * 0.16);
        plane.closePath();
        g.setPaint(Color.WHITE);
        g.fill(plane);
        Path2D wing = new Path2D.Double();
        wing.moveTo(-size * 0.45, 0);
        wing.lineTo(size * 0.2, -size * 0.55);
        wing.lineTo(size * 0.55, -size * 0.5);
        wing.lineTo(size * 0.05, size * 0.02);
        wing.closePath();
        g.setPaint(new Color(0xD9, 0xE4, 0xF7));
        g.fill(wing);
        g.setTransform(old);
    }
}
