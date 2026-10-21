package com.airline.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/** Combo box with a soft chevron instead of the default metal arrow. */
public class SoftComboUI extends BasicComboBoxUI {

    @Override
    protected JButton createArrowButton() {
        JButton b = new JButton();
        b.setUI(new ChevronUI());
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        return b;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BASE);
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        g2.dispose();
        super.paint(g, c);
    }

    /** Paints a small soft chevron. */
    private static class ChevronUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.MUTED);
            int w = c.getWidth(), h = c.getHeight();
            int cx = w - 14, cy = h / 2;
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
            g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
            g2.dispose();
        }
    }
}
