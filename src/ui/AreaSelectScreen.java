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
            new Color(20, 50, 20), new Color(80, 180, 80)));

        areaRow.add(buildAreaCard(frame, "🏚  Dungeon",
            "Skeletons & Dark Mages\nBoss: Dark Mage Lord",
            "Medium",
            new Color(30, 20, 50), new Color(140, 80, 220)));

        areaRow.add(buildAreaCard(frame, "🌋  Volcano",
            "Lava Titans\nBoss: Lava Titan King",
            "Hard",
            new Color(50, 15, 10), new Color(220, 80, 40)));

        g.gridy = 2; add(areaRow, g);
    }

    private JPanel buildAreaCard(MainFrame frame,
                                  String title, String enemies,
                                  String difficulty,
                                  Color bg, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 1),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        card.setPreferredSize(new Dimension(220, 200));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(accent);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel diffLabel = new JLabel("[" + difficulty + "]", SwingConstants.CENTER);
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        diffLabel.setForeground(accent);
        diffLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel enemyLabel = new JLabel(
            "<html><center>" + enemies.replace("\n","<br>") + "</center></html>",
            SwingConstants.CENTER);
        enemyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enemyLabel.setForeground(new Color(180, 180, 200));
        enemyLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(diffLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(enemyLabel);

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