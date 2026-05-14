package ui;

import io.SaveManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainMenuScreen extends JPanel {

    private BufferedImage wallpaper;

    public MainMenuScreen(MainFrame frame) {
        loadWallpaper();                         // ← loads assets/wallpaper/menu_bg.png
        setLayout(new BorderLayout(0, 0));

        // ── TOP NAV STRIP ──
        add(buildTopNav(frame), BorderLayout.NORTH);

        // ── CENTER: title + left content ──
        add(buildCenter(frame), BorderLayout.CENTER);
    }

    // ─────────────────────────────────────
    // BACKGROUND — wallpaper painted behind everything
    // ─────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (wallpaper == null) {
            // No image: fall back to a deep red gradient
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(
                0, 0,           new Color(20, 6, 6),
                getWidth(), getHeight(), new Color(50, 14, 8)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        // Scale wallpaper to fill the panel, preserve aspect ratio, center it
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int pw = getWidth(),  ph = getHeight();
        int iw = wallpaper.getWidth(), ih = wallpaper.getHeight();
        double scale = Math.max((double) pw / iw, (double) ph / ih);
        int dw = (int)(iw * scale), dh = (int)(ih * scale);
        g2.drawImage(wallpaper, (pw - dw) / 2, (ph - dh) / 2, dw, dh, null);

        // Dark vignette overlay so text stays readable
        g2.setPaint(new GradientPaint(
            0, 0, new Color(0, 0, 0, 160),
            pw / 2f, 0, new Color(0, 0, 0, 0)));
        g2.fillRect(0, 0, pw, ph);
        g2.dispose();
    }

    // ─────────────────────────────────────
    // TOP NAV — HOME · LEADERBOARD · CREDITS  (decorative, matches reference)
    // ─────────────────────────────────────
    private JPanel buildTopNav(MainFrame frame) {
        JPanel nav = new JPanel(new BorderLayout(0, 0));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));

        // Left: nav links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        links.setOpaque(false);

        String[] items = {"HOME", "LEADERBOARD", "CREDITS"};
        for (String item : items) {
            JLabel lbl = new JLabel(item);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(new Color(180, 140, 80));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    lbl.setForeground(new Color(240, 192, 96));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    lbl.setForeground(new Color(180, 140, 80));
                }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (item.equals("LEADERBOARD")) frame.goToLeaderboard();
                }
            });
            links.add(lbl);
            if (!item.equals("CREDITS")) {
                JLabel dot = new JLabel("•");
                dot.setFont(new Font("Serif", Font.PLAIN, 8));
                dot.setForeground(new Color(100, 70, 30));
                links.add(dot);
            }
        }

        // Center: game title
        JLabel title = new JLabel("KAKUSA WARS", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28));   // ← change font size here
        title.setForeground(new Color(240, 192, 96));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        // Right: version tag
        JLabel ver = new JLabel("v1.0  ⚔");
        ver.setFont(new Font("Monospaced", Font.PLAIN, 10));
        ver.setForeground(new Color(100, 70, 30, 180));

        nav.add(links,  BorderLayout.WEST);
        nav.add(title,  BorderLayout.CENTER);
        nav.add(ver,    BorderLayout.EAST);
        return nav;
    }

    // ─────────────────────────────────────
    // CENTER CONTENT — big quote (left) + buttons
    // ─────────────────────────────────────
    private JPanel buildCenter(MainFrame frame) {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 44, 60, 44));

        // LEFT COLUMN — quote + welcome + buttons
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Big quote
        JLabel quote = new JLabel(
    "<html><center>FORGE YOUR<br>LEGEND<br>IN BATTLE</center></html>"
);
quote.setFont(new Font("Serif", Font.BOLD, 46));
quote.setForeground(Color.WHITE);
quote.setAlignmentX(LEFT_ALIGNMENT);

        // Welcome text
        JLabel welcome = new JLabel("Welcome, warrior. Your story begins here.");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(new Color(180, 150, 100, 200));
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        welcome.setBorder(BorderFactory.createEmptyBorder(10, 0, 22, 0));

        // ── Button row ──
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton newGameBtn  = makeMenuBtn("⚔  NEW GAME",  new Color(120, 25, 25), new Color(255, 210, 100));
        JButton continueBtn = makeMenuBtn("▶  CONTINUE",  new Color(40, 30, 15),  new Color(180, 140, 70));

        // Disable Continue if no valid save exists
        boolean hasSave = SaveManager.hasSaveData();
        continueBtn.setEnabled(hasSave);
        continueBtn.setForeground(hasSave ? new Color(180, 140, 70) : new Color(80, 60, 30));
        continueBtn.setToolTipText(hasSave ? "Resume your last session" : "No save data found");

        newGameBtn.addActionListener(e -> {
            // Clear any existing save, then go to username entry
            SaveManager.clearSaveData();
            frame.goToUsername();                 // ← UsernameScreen handles name entry
        });

        continueBtn.addActionListener(e -> {
            if (frame.loadSavedGame()) {
                frame.goToAreaSelect();           // ← drop straight back into the game
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Save data is corrupted or missing.",
                    "Load Failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnRow.add(newGameBtn);
        btnRow.add(continueBtn);

        left.add(quote);
        left.add(welcome);
        left.add(btnRow);
        left.add(Box.createVerticalGlue());

        center.add(left, BorderLayout.WEST);
        return center;
    }

    // ─────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────
    private JButton makeMenuBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 80), 1),
            BorderFactory.createEmptyBorder(10, 22, 10, 22)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private void loadWallpaper() {
        // ← wallpaper file path — put your PNG at assets/wallpaper/menu_bg.png
        File f = new File("assets/wallpaper/menu_bg.png");
        if (!f.exists()) return;
        try {
            wallpaper = ImageIO.read(f);
        } catch (IOException ignored) {}
    }
}
