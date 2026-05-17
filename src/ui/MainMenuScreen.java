package ui;

import io.SaveManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
    public void refresh() {
    loadWallpaper(); // Pick a new random file
    revalidate();    // Refresh layout
    repaint();       // Redraw the screen with the new image
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

        Dimension sideWidth = new Dimension(350, 30);

        // Left: nav links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 0));
        links.setOpaque(false);
        links.setPreferredSize(sideWidth);

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
    title.setFont(new Font("Serif", Font.BOLD, 28));
    title.setForeground(new Color(240, 192, 96));
    // No specific width needed; it will take the remaining balanced space

    // ── RIGHT: Version ──
    // Put the version in a panel that matches the width of the left links
    JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightWrapper.setOpaque(false);
    rightWrapper.setPreferredSize(sideWidth); // Force width to match left side

    JLabel ver = new JLabel("v1.0  ⚔");
    ver.setFont(new Font("Monospaced", Font.PLAIN, 10));
    ver.setForeground(new Color(100, 70, 30, 180));
    rightWrapper.add(ver);

    // ── ADD TO NAV ──
    nav.add(links,        BorderLayout.WEST);
    nav.add(title,        BorderLayout.CENTER);
    nav.add(rightWrapper, BorderLayout.EAST);
    
    return nav;
}

    // ─────────────────────────────────────
    // CENTER CONTENT — big quote (left) + buttons
    // ─────────────────────────────────────
    private JPanel buildCenter(MainFrame frame) {
    JPanel center = new JPanel(new BorderLayout(0, 0));
    center.setOpaque(false);
    
    // Padding: T=80, L=60, B=60, R=44 (Keeps text away from screen edges)
    center.setBorder(BorderFactory.createEmptyBorder(80, 60, 60, 44));

    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    left.setOpaque(false);

    // ── 1. BIGGER QUOTE (The Hero Text) ──
    JLabel quote = new JLabel("<html>FORGE YOUR<br>LEGEND<br>IN BATTLE</html>");
    quote.setFont(new Font("Serif", Font.BOLD, 72)); 
    quote.setForeground(Color.WHITE);
    quote.setAlignmentX(LEFT_ALIGNMENT);
    left.add(quote);

    // Small gap between title and welcome sentence
    left.add(Box.createVerticalStrut(20)); 

    // ── 2. BIGGER WELCOME (The Subtitle) ──
    JLabel welcome = new JLabel("Welcome, warrior. Your story begins here.");
    welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    welcome.setForeground(new Color(200, 180, 150));
    welcome.setAlignmentX(LEFT_ALIGNMENT);
    left.add(welcome);

    // ── 3. THE "SPRING" (Vertical Glue) ──
    // This fills ALL the empty space between the text above and buttons below.
    left.add(Box.createVerticalGlue());

    // ── 4. BUTTON ROW ──
    JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    btnRow.setOpaque(false);
    btnRow.setAlignmentX(LEFT_ALIGNMENT);
    // Keep the button row height tight
    btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 

    JButton newGameBtn  = makeMenuBtn("⚔   NEW GAME",  new Color(120, 25, 25), new Color(255, 210, 100));
    JButton continueBtn = makeMenuBtn("▶   CONTINUE",  new Color(40, 30, 15),  new Color(180, 140, 70));

    // Save Data Logic
    boolean hasSave = SaveManager.hasSaveData();
    continueBtn.setEnabled(hasSave);
    continueBtn.setForeground(hasSave ? new Color(180, 140, 70) : new Color(80, 60, 30));
    continueBtn.setToolTipText(hasSave ? "Resume your last session" : "No save data found");

    newGameBtn.addActionListener(e -> {
        SaveManager.clearSaveData();
        frame.goToUsername();
    });

    continueBtn.addActionListener(e -> {
        if (frame.loadSavedGame()) {
            frame.goToBattle();
        } else {
            JOptionPane.showMessageDialog(frame, "Save data is corrupted or missing.", "Load Failed", JOptionPane.WARNING_MESSAGE);
        }
    });

    btnRow.add(newGameBtn);
    btnRow.add(continueBtn);
    
    // Add the button row to the left panel
    left.add(btnRow);

    // ── 5. BOTTOM BREATHING ROOM ──
    // Prevents buttons from being stuck to the very bottom edge
    left.add(Box.createVerticalStrut(20)); 

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
    // 1. Define the pairs: "Wallpaper_Filename" -> "Music_Filename"
    // Tip: Make sure these names match your files exactly!
    Map<String, String> themes = new HashMap<>();
    themes.put("menu_bg.png",      "adventure.wav");
    themes.put("green_bg.png",    "soft.wav");
    themes.put("dragon_bg.png",    "dragon.wav");
    themes.put("desert_bg.png",    "desert.wav");
    themes.put("gold_bg.png",    "tribe.wav");
    themes.put("arthur.png",    "arthur.wav");


    // 2. Convert the map keys to a list so we can pick one randomly
    List<String> wallpaperNames = new ArrayList<>(themes.keySet());
    
    if (!wallpaperNames.isEmpty()) {
        // 3. Pick a random theme
        int randomIndex = new java.util.Random().nextInt(wallpaperNames.size());
        String selectedImgName = wallpaperNames.get(randomIndex);
        String selectedMusic = themes.get(selectedImgName);

        try {
            // 4. Load the Image
            File imgFile = new File("assets/wallpaper/" + selectedImgName);
            if (imgFile.exists()) {
                this.wallpaper = ImageIO.read(imgFile);
            }

            // 5. Play the synced Music
            // We call our MusicManager here so the song matches the vibe
            io.MusicManager.play(selectedMusic, true);

        } catch (IOException e) {
            System.err.println("Theme Load Error: " + e.getMessage());
            // Fallback: If specific music fails, play a default
            io.MusicManager.play("menu_theme.wav", true);
        }
    }
}
}
