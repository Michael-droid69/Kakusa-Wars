package ui;

import javax.swing.*;
import java.awt.*;

public class GameOverScreen extends JPanel {

    public GameOverScreen(MainFrame frame, boolean victory) {
        setLayout(new GridBagLayout());
        setBackground(victory ? new Color(8, 18, 10) : new Color(20, 8, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(18, 0, 18, 0);

        // ── Title ──
        String titleText = victory ? "🏆  VICTORY!" : "💀  DEFEAT";
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(victory
            ? new Color(100, 240, 130)
            : new Color(220, 60,  60));
        g.gridy = 0; add(title, g);

        // ── Subtitle ──
        String sub = victory
            ? "The realm is saved, " + frame.getUsername() + "!"
            : "You fought bravely, " + frame.getUsername() + ". Try again.";
        JLabel subLabel = new JLabel(sub, SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subLabel.setForeground(new Color(180, 180, 200));
        g.gridy = 1; add(subLabel, g);

        // ── Score panel ──
        JPanel scoreBox = new JPanel(new GridLayout(3, 2, 20, 8));
        scoreBox.setBackground(new Color(20, 20, 30));
        scoreBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(20, 40, 20, 40)));

        addScoreRow(scoreBox, "Area Reached:",    frame.getCurrentArea());
        addScoreRow(scoreBox, "Enemies Defeated:", String.valueOf(frame.getEnemiesKilled()));
        addScoreRow(scoreBox, "Total Turns:",      String.valueOf(frame.getTurnsTotal()));

        g.gridy = 2; add(scoreBox, g);

        // ── Buttons ──
        JButton playAgain = makeBtn("▶   Play Again",
            new Color(50, 25, 90), Color.WHITE);
        playAgain.addActionListener(e -> {
            // Clear save and restart from username screen
            try { io.SaveManager.deleteSave(); } catch (Exception ex) { /* ignore */ }
            frame.setCurrentWave(1);
            frame.goToUsername();
        });
        g.gridy = 3; add(playAgain, g);

        // If victory, delete save (run is complete)
        if (victory) {
            try { io.SaveManager.deleteSave(); } catch (java.io.IOException ex) { /* ignore */ }
        }
    }

    private void addScoreRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        l.setForeground(new Color(160, 160, 190));

        JLabel v = new JLabel(value, SwingConstants.LEFT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 15));
        v.setForeground(new Color(240, 210, 100));

        panel.add(l);
        panel.add(v);
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 44));
        return btn;
    }
}