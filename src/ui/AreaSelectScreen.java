package ui;

import javax.swing.*;
import java.awt.*;

public class AreaSelectScreen extends JPanel {

    public AreaSelectScreen(MainFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(10, 10, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(16, 0, 16, 0);

        JLabel header = new JLabel("Choose Your Battlefield");
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(new Color(240, 192, 96));
        g.gridy = 0; add(header, g);

        JLabel sub = new JLabel("4 waves per area. Wave 4 is always the boss.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(130, 130, 160));
        g.gridy = 1; add(sub, g);

        // ── 3 area cards side by side ──
        JPanel areaRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        areaRow.setBackground(new Color(10, 10, 16));

        areaRow.add(buildAreaCard(frame, "🌲  Forest",
    "Goblins & Trolls\nBoss: Dragon",
    "Easy",
    new Color(20, 50, 20), new Color(80, 180, 80),
    "assets/landscapes/forest_bg.png"));

        areaRow.add(buildAreaCard(frame, "🏚  Academia",
    "Skeletons & Dark Mages\nBoss: Dark Mage Lord",
    "Medium",
    new Color(30, 20, 50), new Color(140, 80, 220),
    "assets/landscapes/academia_bg.png"));

        areaRow.add(buildAreaCard(frame, "🌋  Dungeon",
    "Lava Titans\nBoss: Lava Titan King",
    "Hard",
    new Color(50, 15, 10), new Color(220, 80, 40),
    "assets/landscapes/dungeon_bg.png"));

        g.gridy = 2; add(areaRow, g);
    }

    private JPanel buildAreaCard(MainFrame frame, String title, String enemies, 
                             String difficulty, Color bg, Color accent, String imagePath) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(bg);
    
    
    card.setBorder(BorderFactory.createLineBorder(accent, 1));
    card.setPreferredSize(new Dimension(240, 320)); // Made it taller for the image
    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // 1. THE IMAGE BOX
    JLabel imageLabel = new JLabel();
    try {
        ImageIcon icon = new ImageIcon(imagePath);
        // Resize image to fit the top of the card (240 width, 140 height)
        Image scaled = icon.getImage().getScaledInstance(240, 140, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
    } catch (Exception e) {
        imageLabel.setText("Image Missing");
        imageLabel.setForeground(Color.GRAY);
    }
    imageLabel.setAlignmentX(CENTER_ALIGNMENT);
    
    // 2. THE TEXT CONTAINER (Adding padding so text doesn't touch edges)
    JPanel textContainer = new JPanel();
    textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
    textContainer.setOpaque(false); // Let the card's background show through
    textContainer.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    titleLabel.setForeground(accent);
    titleLabel.setAlignmentX(CENTER_ALIGNMENT);

    JLabel diffLabel = new JLabel(difficulty.toUpperCase());
    diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
    diffLabel.setForeground(accent.brighter());
    diffLabel.setAlignmentX(CENTER_ALIGNMENT);

    JLabel enemyLabel = new JLabel("<html><center>" + enemies.replace("\n","<br>") + "</center></html>");
    enemyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    enemyLabel.setForeground(new Color(180, 180, 200));
    enemyLabel.setAlignmentX(CENTER_ALIGNMENT);

    // Assembly
    textContainer.add(titleLabel);
    textContainer.add(Box.createVerticalStrut(5));
    textContainer.add(diffLabel);
    textContainer.add(Box.createVerticalStrut(10));
    textContainer.add(enemyLabel);

    card.add(imageLabel);      // Image on top
    card.add(textContainer);   // Text below

     // Extract clean area name from the title string for setCurrentArea

        String areaName = title.replaceAll("[^a-zA-Z]", "").trim();



        card.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override public void mouseClicked(java.awt.event.MouseEvent e) {

                frame.setCurrentArea(areaName);

                frame.setCurrentWave(1);

                frame.goToBattle();

            }

            @Override public void mouseEntered(java.awt.event.MouseEvent e) {

                card.setBackground(bg.brighter());

            }

            @Override public void mouseExited(java.awt.event.MouseEvent e) {

                card.setBackground(bg);

            }
        });
    
    return card;
}
}