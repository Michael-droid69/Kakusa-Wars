package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectScreen extends JPanel {

    private final List<core.Character> selected = new ArrayList<>();
    private final List<JPanel>         cards     = new ArrayList<>();

    // ── Colours ──
    private static final Color BG          = new Color(10, 10, 16);
    private static final Color GOLD        = new Color(240, 192, 96);
    private static final Color BORDER_IDLE = new Color(60, 40, 90);
    private static final Color BORDER_SEL  = new Color(160, 80, 255);

    public CharacterSelectScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG);

        // ── Header ──
        JLabel header = new JLabel("Select Your 2 Heroes", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(GOLD);
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        add(header, BorderLayout.NORTH);

        // ── Cards area (scroll) ──
        // Switch to horizontal scrolling (left/right) for better placement.
        JPanel cardsPanel = new JPanel();
        cardsPanel.setBackground(BG);
        cardsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 0));


        JPanel cardRow = new JPanel();
        cardRow.setBackground(BG);
        cardRow.setLayout(new BoxLayout(cardRow, BoxLayout.X_AXIS));
        cardRow.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        List<core.Character> pool = List.of(
            new characters.Arthur("Arthur"),
            new characters.Fabby("Fabby"),
            new characters.Mohammad("Mohammad"),
            new characters.Star("Star"),
            new characters.Tapanh("Tapanh"),
            new characters.Van("Van")
        );

        // We want "only accommodate 3 per screen". With a scrollable Y container,
        // we can emulate this by using a 3-wide row layout.
// Since there are 6 cards total, using 2 columns will give 3 rows.
// This matches your preference: "2 cards per screen" with visible gaps.
        for (core.Character c : pool) {
    JPanel card = buildCard(c, frame);
    cards.add(card);
    cardsPanel.add(card);  // add directly to cardsPanel, no row splitting
}

        // Height tuned so user sees about 3 cards (large ones) at once,
        // while still allowing vertical scroll.
        JScrollPane scrollPane = new JScrollPane(
            cardsPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED 
        );
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(BG);

        // Prefer size so the viewport matches "3 cards per screen" feel.
        // (If the frame window is smaller, scrollbar will appear.)
        scrollPane.setPreferredSize(new Dimension(1020, 510));

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBackground(BG);
        cardsWrapper.add(scrollPane, BorderLayout.CENTER);

        add(cardsWrapper, BorderLayout.CENTER);

        // ── Confirm button ──
        JButton confirm = new JButton("Confirm Selection  \u25B6");
        confirm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        confirm.setBackground(new Color(60, 25, 100));
        confirm.setForeground(Color.WHITE);
        confirm.setFocusPainted(false);
        confirm.setBorderPainted(false);
        confirm.setPreferredSize(new Dimension(240, 44));
        confirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bottom = new JPanel();
        bottom.setBackground(BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        bottom.add(confirm);
        add(bottom, BorderLayout.SOUTH);

        confirm.addActionListener(e -> {
            if (selected.size() != 2) {
                JOptionPane.showMessageDialog(frame,
                    "You must select exactly 2 heroes!",
                    "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            frame.setParty(new ArrayList<>(selected));
            frame.goToAreaSelect();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  buildCard — portrait image + overlay + clean text layout
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCard(core.Character c, MainFrame frame) {

        // Slightly narrower card for better spacing and a cleaner grid.
        final int W = 270, H = 480;

        // Load the portrait from assets/portraits/<name_lowercase>.png
        BufferedImage portrait = loadPortrait(c.getName());

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // 1 ── Background: portrait or dark fallback
                if (portrait != null) {
                    g2.drawImage(portrait, 0, 0, w, h, null);
                } else {
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(30, 15, 50),
                        0, h, new Color(10, 10, 20));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w, h, 12, 12);

                    // "No image" hint
                    g2.setColor(new Color(80, 80, 100));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String hint = "assets/portraits/";
                    String hint2 = c.getName().toLowerCase() + ".png";
                    g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2 - 8);
                    g2.drawString(hint2, (w - fm.stringWidth(hint2)) / 2, h / 2 + 10);
                }

                // 2 ── Bottom gradient overlay (dark fade for text readability)
                int fadeH = (int) (h * 0.60);
                GradientPaint fade = new GradientPaint(
                    0, h - fadeH, new Color(0, 0, 0, 0),
                    0, h, new Color(0, 0, 0, 235));
                g2.setPaint(fade);
                g2.fillRect(0, h - fadeH, w, fadeH);

                // 3 ── Text: name + class line + stats (NO overlaps)
                // Keep the text inside the bottom overlay area with extra padding.
                final int padX = 16;
                final int topTextY = h - fadeH + 10;
                final int bottomTextY = h - 30;

                int contentH = bottomTextY - topTextY;
                if (contentH < 1) contentH = 1;

                // Text fonts
                Font nameFont = new Font("Segoe UI", Font.BOLD, 18);
                Font classFont = new Font("Segoe UI", Font.PLAIN, 12);
                Font statsFont = new Font("Segoe UI", Font.PLAIN, 12);

                // Colors
                Color nameColor = GOLD;
                Color classColor = new Color(170, 170, 220);
                Color statsColor = new Color(205, 205, 230);

                // Measure heights
                g2.setFont(nameFont);
                FontMetrics fmName = g2.getFontMetrics();
                int nameH = fmName.getHeight();

                g2.setFont(classFont);
                FontMetrics fmClass = g2.getFontMetrics();
                int classH = fmClass.getHeight();

                g2.setFont(statsFont);
                FontMetrics fmStats = g2.getFontMetrics();
                int lineH = fmStats.getHeight();

                // Stats occupy 3 lines max area; we keep them predictable.
                // Two-column second line? We'll use three single lines to avoid clutter.
                // Example:
                // HP .. MP ..
                // ATK .. DEF ..
                // SPD ..
                String hpMp = "HP " + c.getMaxHp() + "   MP " + c.getMaxMana();
                String atkDef = "ATK " + c.getAttackPower() + "   DEF " + c.getDefensePower();
                String spd = "SPD " + c.getSpeed();

                // Truncate helper: fit text width within w - 2*padX
                int maxTextW = w - padX * 2;
                String classLineRaw = c.getClassDescription().split("\n")[0];
                String classLine = fitToWidth(g2, classFont, classLineRaw, maxTextW);

                String name = fitToWidth(g2, nameFont, c.getName(), maxTextW);

                boolean sel = selected.contains(c);

                // Vertical placement: stack with spacing, adjust to contentH.
                int spacing1 = 6;
                int spacing2 = 6;
                int totalNeeded = nameH + spacing1 + classH + spacing2 + (lineH * 3);

                int startY = topTextY;
                if (totalNeeded < contentH) {
                    // Center within content box for cleanliness.
                    startY = topTextY + (contentH - totalNeeded) / 2;
                }

                int y = startY;

                // Name
                g2.setFont(nameFont);
                g2.setColor(nameColor);
                g2.drawString(name, padX, y + fmName.getAscent());
                y += nameH + spacing1;

                // Class line
                g2.setFont(classFont);
                g2.setColor(classColor);
                g2.drawString(classLine, padX, y + fmClass.getAscent());
                y += classH + spacing2;

                // Stats lines
                g2.setFont(statsFont);
                g2.setColor(statsColor);

                g2.drawString(fitToWidth(g2, statsFont, hpMp, maxTextW), padX, y + fmStats.getAscent());
                y += lineH;

                g2.drawString(fitToWidth(g2, statsFont, atkDef, maxTextW), padX, y + fmStats.getAscent());
                y += lineH;

                g2.drawString(fitToWidth(g2, statsFont, spd, maxTextW), padX, y + fmStats.getAscent());

                // 4 ── Border (idle = faint purple, selected = bright purple glow)
                g2.setColor(sel ? BORDER_SEL : BORDER_IDLE);
                g2.setStroke(new BasicStroke(sel ? 2.5f : 1.5f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 12, 12);

                // 5 ── "✓" badge when selected
                if (sel) {
                    g2.setColor(new Color(160, 80, 255));
                    g2.fillRoundRect(w - 62, 10, 52, 20, 9, 9);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString("\u2713 Pick", w - 56, 24);
                }
            }
        };

        card.setPreferredSize(new Dimension(W, H));
        card.setMaximumSize(new Dimension(W, H));
        card.setMinimumSize(new Dimension(W, H));
        card.setOpaque(false);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Click to toggle selection
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selected.contains(c)) {
                    selected.remove(c);
                } else if (selected.size() < 2) {
                    selected.add(c);
                } else {
                    JOptionPane.showMessageDialog(frame,
                        "You can only pick 2 heroes. Deselect one first.",
                        "Max 2", JOptionPane.INFORMATION_MESSAGE);
                }
                card.repaint();
            }

            @Override public void mouseEntered(java.awt.event.MouseEvent e) { card.repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { card.repaint(); }
        });

        return card;
    }

    // Fit text to width with "..." so it never overlaps.
    private static String fitToWidth(Graphics2D g2, Font font, String text, int maxW) {
        if (text == null) return "";
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) return text;

        String ell = "...";
        int ellW = fm.stringWidth(ell);
        int avail = Math.max(0, maxW - ellW);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (fm.stringWidth(sb.toString()) > avail) {
                sb.setLength(Math.max(0, sb.length() - 1));
                break;
            }
        }
        return sb + ell;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  loadPortrait — reads assets/portraits/<name>.png, returns null if missing
    // ─────────────────────────────────────────────────────────────────────────
    private BufferedImage loadPortrait(String characterName) {
        String path = "assets/portraits/" + characterName.toLowerCase() + ".png";
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (IOException ignored) {}
        return null;  // card will show placeholder text instead
    }
}
