package ui;

import javax.swing.*;
import java.awt.*;

public class HealthBar extends JComponent {

    private core.Character character;
    private final int barWidth;
    private final int barHeight;

    // character: who this bar belongs to
    // barWidth/barHeight: how big the bar is in pixels
    public HealthBar(core.Character character, int barWidth, int barHeight) {
        this.character = character;
        this.barWidth = barWidth;
        this.barHeight = barHeight;
        setPreferredSize(new Dimension(barWidth, barHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark red background (empty bar)
        g2.setColor(new Color(50, 10, 10));
        g2.fillRoundRect(0, 0, barWidth, barHeight, 10, 10);

        // How full the bar is as a fraction (0.0 to 1.0)
        double ratio = (double) character.getHp() / character.getMaxHp();

        // Color changes based on HP level
        Color fillColor;
        if (ratio > 0.5) {
            fillColor = new Color(50, 200, 80);   // green — healthy
        } else if (ratio > 0.25) {
            fillColor = new Color(230, 170, 20);  // yellow — caution
        } else {
            fillColor = new Color(220, 40, 40);   // red — danger
        }

        // Draw the filled portion
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, (int) (barWidth * ratio), barHeight, 10, 10);

        // Bar outline
        g2.setColor(new Color(90, 90, 90));
        g2.drawRoundRect(0, 0, barWidth - 1, barHeight - 1, 10, 10);

        // HP text in the center of the bar
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String text = character.getHp() + " / " + character.getMaxHp();
        FontMetrics fm = g2.getFontMetrics();
        int textX = (barWidth - fm.stringWidth(text)) / 2;
        int textY = (barHeight + fm.getAscent()) / 2 - 2;
        g2.drawString(text, textX, textY);
    }

    // Call this after any HP change, then call repaint() on the parent panel
    public void update(core.Character updatedCharacter) {
        this.character = updatedCharacter;
        repaint();
    }
}
