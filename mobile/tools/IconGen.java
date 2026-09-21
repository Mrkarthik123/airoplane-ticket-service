import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Generates SkyJet app icons (paper-plane on navy gradient). Usage: java IconGen <outDir> */
public class IconGen {
    public static void main(String[] a) throws Exception {
        File out = new File(a.length > 0 ? a[0] : ".");
        out.mkdirs();
        draw(new File(out, "icon-192.png"), 192, false);
        draw(new File(out, "icon-512.png"), 512, false);
        draw(new File(out, "maskable-512.png"), 512, true);
        draw(new File(out, "apple-touch-icon.png"), 180, false);
        System.out.println("icons written to " + out.getAbsolutePath());
    }

    static void draw(File f, int s, boolean maskable) throws Exception {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int inset = maskable ? s / 8 : 0; // safe zone for maskable
        // navy gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(0x0f172a), s, s, new Color(0x0369a1));
        g.setPaint(gp);
        if (maskable) g.fillRect(0, 0, s, s);
        else { g.setClip(new RoundRectangle2D.Float(0, 0, s, s, s * 0.24f, s * 0.24f)); g.fillRect(0, 0, s, s); g.setClip(null); }
        // sky circle
        g.setColor(new Color(0x38bdf8));
        int r = s * 3 / 8;
        g.fillOval((s - r) / 2, (s - r) / 2 - s / 32, r, r);
        // paper plane (white polygon)
        double cx = s / 2.0, cy = s / 2.0 - s / 64.0, w = s * 0.21, h = s * 0.17;
        Polygon plane = new Polygon();
        plane.addPoint((int) (cx - w), (int) (cy - h * 0.5));
        plane.addPoint((int) (cx + w * 1.1), (int) cy);
        plane.addPoint((int) (cx - w), (int) (cy + h * 0.5));
        plane.addPoint((int) (cx - w * 0.45), (int) cy);
        g.setColor(Color.WHITE);
        g.fillPolygon(plane);
        // wordmark bar
        g.setColor(Color.WHITE);
        int bw = (int) (s * 0.44), bh = Math.max(2, s / 42);
        g.fillRoundRect((s - bw) / 2, (int) (s * 0.74), bw, bh, bh, bh);
        g.dispose();
        ImageIO.write(img, "png", f);
    }
}
