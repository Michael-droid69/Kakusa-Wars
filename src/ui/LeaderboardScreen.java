package ui;

import io.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LeaderboardScreen extends JPanel {

    public LeaderboardScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 16));
        setBackground(new Color(8, 6, 14));

        // ── Header ──
        JLabel header = new JLabel("⚔  LEADERBOARD  ⚔", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 26));
        header.setForeground(new Color(240, 192, 96));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        add(header, BorderLayout.NORTH);

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
        table.setBackground(new Color(12, 10, 20));
        table.setForeground(new Color(200, 190, 230));
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setGridColor(new Color(40, 35, 60));
        table.getTableHeader().setBackground(new Color(25, 18, 40));
        table.getTableHeader().setForeground(new Color(180, 140, 80));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Gold colour for top 3
        table.setDefaultRenderer(Object.class, (t, value, sel, foc, row, col) -> {
            JLabel cell = new JLabel(value != null ? value.toString() : "");
            cell.setFont(new Font("Monospaced", Font.PLAIN, 13));
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            cell.setBackground(row % 2 == 0 ? new Color(12,10,20) : new Color(18,14,28));
            cell.setForeground(row == 0 ? new Color(255, 210, 80)
                             : row == 1 ? new Color(200, 200, 200)
                             : row == 2 ? new Color(180, 130, 80)
                             : new Color(160, 155, 190));
            return cell;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(new Color(12, 10, 20));
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        add(scroll, BorderLayout.CENTER);

        // ── Back button ──
        JButton back = new JButton("← Back to Menu");
        back.setFont(new Font("Segoe UI", Font.BOLD, 12));
        back.setBackground(new Color(30, 22, 8));
        back.setForeground(new Color(180, 140, 70));
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> frame.goToMainMenu());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(8, 6, 14));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }
}
