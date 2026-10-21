package com.airline.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Airport locations, map painting and the airport list panel. */
public class AirportsPanel extends JPanel {
    /** code -> [city, airport name, lat, lng] */
    public static final Map<String, String[]> DATA = new LinkedHashMap<>();
    static {
        DATA.put("DEL", new String[]{"New Delhi", "Indira Gandhi International Airport", "28.556", "77.100"});
        DATA.put("BOM", new String[]{"Mumbai", "Chhatrapati Shivaji Maharaj International", "19.090", "72.865"});
        DATA.put("BLR", new String[]{"Bengaluru", "Kempegowda International Airport", "13.198", "77.706"});
        DATA.put("HYD", new String[]{"Hyderabad", "Rajiv Gandhi International Airport", "17.231", "78.429"});
        DATA.put("MAA", new String[]{"Chennai", "Chennai International Airport", "12.994", "80.171"});
    }

    private static final String OUTLINE =
            "109,4 138,18 168,14 196,30 228,32 252,52 262,80 288,96 296,124 282,150 292,178 276,204 282,232 " +
            "258,258 240,292 222,330 206,368 188,410 172,438 158,410 146,372 128,338 104,308 78,282 52,258 " +
            "30,232 14,206 22,182 12,158 28,132 24,104 48,88 60,60 84,44";

    public interface OnPick { void pick(String code); }

    private final OnPick onPick;
    private final MapPanel map;

    public AirportsPanel(OnPick onPick) {
        this.onPick = onPick;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(Theme.pad(14, 14, 14, 14));

        map = new MapPanel();
        JPanel mapCard = Theme.card(new BorderLayout());
        mapCard.setBorder(Theme.pad(10, 10, 10, 10));
        mapCard.add(map, BorderLayout.CENTER);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.add(Theme.title("Served airports", 17));
        list.add(Box.createVerticalStrut(4));
        list.add(Theme.muted("Click a marker on the map or pick a city to search flights from it.", 12));
        list.add(Box.createVerticalStrut(10));
        for (Map.Entry<String, String[]> e : DATA.entrySet()) list.add(airportCard(e.getKey(), e.getValue()));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(mapCard, BorderLayout.CENTER);
        add(scroll, BorderLayout.EAST);
        mapCard.setPreferredSize(new Dimension(460, 520));
        scroll.setPreferredSize(new Dimension(360, 520));
    }

    private JPanel airportCard(String code, String[] d) {
        SoftPanel p = new SoftPanel(SoftPanel.Style.RAISED, new BorderLayout(10, 4));
        p.setBorder(Theme.pad(12, 14, 12, 14));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JPanel left = new JPanel(new GridLayout(0, 1));
        left.setOpaque(false);
        JLabel codeL = Theme.title(code + "  ·  " + d[0], 15);
        JLabel meta = Theme.muted(d[1] + "  ·  " + d[2] + "°N, " + d[3] + "°E", 11);
        left.add(codeL);
        left.add(meta);

        JButton go = Theme.button("Fly from here");
        go.addActionListener(e -> onPick.pick(code));

        p.add(left, BorderLayout.CENTER);
        p.add(go, BorderLayout.EAST);
        return p;
    }

    /** Painted map of India with airport markers and route network. */
    static class MapPanel extends JPanel {
        private String selected = null;
        private final Timer timer;
        private double pulse = 0;

        MapPanel() {
            setPreferredSize(new Dimension(420, 480));
            setBackground(new Color(0xE3ECF9));
            timer = new Timer(40, e -> { pulse = (pulse + 0.05) % 1.0; repaint(); });
            timer.start();
        }

        void select(String code) { selected = code; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            int w = getWidth(), h = getHeight();
            double sx = w / 400.0, sy = h / 440.0;

            // sea
            g2.setPaint(new GradientPaint(0, 0, new Color(0xD3E1F7), 0, h, new Color(0xEAF1FC)));
            g2.fillRect(0, 0, w, h);

            // land
            Path2D land = new Path2D.Double();
            String[] pts = OUTLINE.split(" ");
            for (int i = 0; i < pts.length; i++) {
                String[] xy = pts[i].split(",");
                double x = Double.parseDouble(xy[0]) * sx, y = Double.parseDouble(xy[1]) * sy;
                if (i == 0) land.moveTo(x, y); else land.lineTo(x, y);
            }
            land.closePath();
            g2.setPaint(new Color(0xF5F8FD));
            g2.fill(land);
            g2.setPaint(Theme.SHADOW);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(land);

            // routes
            List<String> codes = new ArrayList<>(DATA.keySet());
            g2.setColor(new Color(Theme.PRIMARY.getRed(), Theme.PRIMARY.getGreen(), Theme.PRIMARY.getBlue(), 120));
            g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5, 4}, 0));
            for (int i = 0; i < codes.size(); i++) {
                for (int j = i + 1; j < codes.size(); j++) {
                    Point2D a = project(codes.get(i), sx, sy), b = project(codes.get(j), sx, sy);
                    g2.draw(new Line2D.Double(a, b));
                }
            }

            // markers
            for (String code : codes) {
                Point2D p = project(code, sx, sy);
                boolean sel = code.equals(selected);
                double r = sel ? 9 : 6;
                g2.setPaint(new Color(Theme.PRIMARY.getRed(), Theme.PRIMARY.getGreen(), Theme.PRIMARY.getBlue(), 50));
                double pr = 10 + pulse * 12;
                g2.fillOval((int) (p.getX() - pr), (int) (p.getY() - pr), (int) (pr * 2), (int) (pr * 2));
                g2.setPaint(sel ? Theme.PRIMARY_DARK : Theme.PRIMARY);
                g2.fillOval((int) (p.getX() - r), (int) (p.getY() - r), (int) (r * 2), (int) (r * 2));
                g2.setPaint(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval((int) (p.getX() - r), (int) (p.getY() - r), (int) (r * 2), (int) (r * 2));
                g2.setFont(Theme.bold(12));
                g2.setColor(Theme.INK);
                String label = code;
                int tw = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, (int) (p.getX() - tw / 2), (int) (p.getY() - r - 7));
            }
            g2.dispose();
        }

        private Point2D project(String code, double sx, double sy) {
            String[] d = DATA.get(code);
            double lat = Double.parseDouble(d[2]), lng = Double.parseDouble(d[3]);
            double x = (lng - 66) / (92 - 66) * 400 * sx;
            double y = (36 - lat) / (36 - 7) * 440 * sy;
            return new Point2D.Double(x, y);
        }
    }
}
