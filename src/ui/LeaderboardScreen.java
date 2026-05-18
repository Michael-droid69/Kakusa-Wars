package ui;

import io.SaveManager;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class LeaderboardScreen extends JPanel {
    
    private BufferedImage backgroundImage;

    public LeaderboardScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        
        // Load tavern background
        loadBackgroundImage();

        // ── LEFT SIDE: Leaderboard Container ──
        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        leftPanel.setPreferredSize(new Dimension(700, 0)); // Fixed width for left side

        // ── Header ──
        JLabel header = new JLabel("⚔  LEADERBOARD  ⚔", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 28));
        header.setForeground(new Color(255, 220, 120));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        leftPanel.add(header, BorderLayout.NORTH);

        // ── Table Container with semi-transparent background ──
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(new Color(20, 15, 10, 220)); // Semi-transparent dark brown
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 50), 3),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // ── Table ──
        String[] columns = {"#", "Username", "Best Wave", "Kills", "Turns", "Last Area"};
        List<String[]> data = SaveManager.loadLeaderboard();

        Object[][] rows = new Object[data.size()][6];
        for (int i = 0; i < data.size(); i++) {
            String[] p = data.get(i);
            rows[i][0] = i + 1;           // rank
            rows[i][1] = p[0];            // username
            rows[i][2] = p[1];            // highest wave
            rows[i][3] = p[2];            // kills
            rows[i][4] = p[3];            // turns
            rows[i][5] = p.length >= 5 ? p[4] : "—"; // last area
        }

        JTable table = new JTable(rows, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setBackground(new Color(30, 22, 15, 200));
        table.setForeground(new Color(230, 220, 200));
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(80, 60, 40));
        table.getTableHeader().setBackground(new Color(50, 35, 20));
        table.getTableHeader().setForeground(new Color(220, 180, 100));
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 13));

        // Gold colour for top 3
        table.setDefaultRenderer(Object.class, (t, value, sel, foc, row, col) -> {
            JLabel cell = new JLabel(value != null ? value.toString() : "");
            cell.setFont(new Font("Monospaced", Font.PLAIN, 13));
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            cell.setBackground(row % 2 == 0 ? new Color(30,22,15,200) : new Color(40,30,20,200));
            cell.setForeground(row == 0 ? new Color(255, 215, 0)      // Gold
                             : row == 1 ? new Color(192, 192, 192)    // Silver
                             : row == 2 ? new Color(205, 127, 50)     // Bronze
                             : new Color(220, 210, 190));             // Normal
            if (row < 3) {
                cell.setFont(new Font("Monospaced", Font.BOLD, 14));
            }
            return cell;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableContainer.add(scroll, BorderLayout.CENTER);

        leftPanel.add(tableContainer, BorderLayout.CENTER);

        // ── Back button ──
        JButton back = new JButton("← Back to Menu");
        back.setFont(new Font("Serif", Font.BOLD, 14));
        back.setBackground(new Color(80, 50, 20));
        back.setForeground(new Color(255, 220, 150));
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 50), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> frame.goToMainMenu());
        
        // Hover effect
        back.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                back.setBackground(new Color(100, 70, 30));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                back.setBackground(new Color(80, 50, 20));
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottom.add(back);
        leftPanel.add(bottom, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("assets/wallpaper/tavern.png"));
        } catch (IOException e) {
            System.out.println("Could not load tavern.png background: " + e.getMessage());
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Draw centered and cropped (no stretch)
            int imgW = backgroundImage.getWidth();
            int imgH = backgroundImage.getHeight();
            int panelW = getWidth();
            int panelH = getHeight();

            // Scale to fill while keeping aspect ratio (crop excess)
            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int)(imgW * scale);
            int drawH = (int)(imgH * scale);

            // Center the image
            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            g.drawImage(backgroundImage, x, y, drawW, drawH, this);
        } else {
            g.setColor(new Color(20, 15, 10));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
