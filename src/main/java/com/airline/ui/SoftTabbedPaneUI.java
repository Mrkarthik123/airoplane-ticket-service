package com.airline.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

/**
 * Tabbed pane whose tabs are soft raised surfaces: the selected tab is raised
 * and highlighted, the others sit flat on the background.
 */
public class SoftTabbedPaneUI extends BasicTabbedPaneUI {

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                            Rectangle iconRect, Rectangle textRect) {
        Rectangle tabRect = rects[tabIndex];
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = tabRect.x, y = tabRect.y, w = tabRect.width, h = tabRect.height;
        boolean selected = tabPane != null && tabPane.getSelectedIndex() == tabIndex;
        int rx = x + 4, ry = y + 4, rw = w - 8, rh = h - 12;

        g2.setColor(Theme.BASE);
        g2.fillRoundRect(rx, ry, rw, rh, 14, 14);
        if (selected && rw > 0 && rh > 0) {
            Soft.outerLight(g2, rx, ry, rw, rh, 14, Theme.HIGHLIGHT, 3, 1, 1, 150);
            Soft.outerShadow(g2, rx, ry, rw, rh, 14, Theme.SHADOW, 4, 2, 2, 110);
        }

        JTabbedPane tp = tabPane;
        String text = tp == null ? "" : tp.getTitleAt(tabIndex);
        Font font = tp == null ? null : tp.getFont();
        if (font == null) font = UIManager.getFont("TabbedPane.font");
        if (font == null) font = new Font("SansSerif", Font.BOLD, 13);
        FontMetrics fm = g2.getFontMetrics(font);
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h - 10 - fm.getHeight()) / 2 + fm.getAscent();
        g2.setFont(font);
        g2.setColor(selected ? Theme.PRIMARY : Theme.MUTED);
        g2.drawString(text, tx, ty);
        g2.dispose();
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                      int x, int y, int w, int h, boolean isSelected) {
        // the tab surface is painted by paintTab
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        // no hairline borders in a soft interface
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // the content sits directly on the background
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                       Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // the raised selected tab is the focus indicator
    }
}
