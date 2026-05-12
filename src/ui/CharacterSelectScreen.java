package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CharacterSelectScreen extends JPanel {

    // Tracks which characters the player selected (max 2)
    private final List<core.Character> selected = new ArrayList<>();
    private final List<JPanel>         cards     = new ArrayList<>();
    private final Color COLOR_SELECTED   = new Color(80, 50, 140);
    private final Color COLOR_UNSELECTED = new Color(22, 22, 32);

    public CharacterSelectScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(10, 10, 16));

        // ── Header ──
        JLabel header = new JLabel("Select Your 2 Heroes", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(new Color(240, 192, 96));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        add(header, BorderLayout.NORTH);

        // ── Cards row ──
        JPanel cardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardRow.setBackground(new Color(10, 10, 16));

        // Build one card per character (use only existing character classes)
        List<core.Character> pool = List.of(
            new characters.Arthur("Arthur"),
            new characters.Fabby("Fabby"),
            new characters.Mohammad("Mohammad"),
            new characters.Star("Star"),
            new characters.Tapanh("Tapanh"),
            new characters.Van("Van")
        );

        for (core.Character c : pool) {
            JPanel card = buildCard(c, frame);
            cards.add(card);
            cardRow.add(card);
        }
        add(cardRow, BorderLayout.CENTER);

        // ── Confirm button ──
        JButton confirm = new JButton("Confirm Selection  ▶");
        confirm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        confirm.setBackground(new Color(60, 25, 100));
        confirm.setForeground(Color.WHITE);
        confirm.setFocusPainted(false);
        confirm.setBorderPainted(false);
        confirm.setPreferredSize(new Dimension(240, 44));
        confirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(10, 10, 16));
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

    private JPanel buildCard(core.Character c, MainFrame frame) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_UNSELECTED);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        card.setPreferredSize(new Dimension(180, 280));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Character name
        JLabel name = new JLabel(c.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(new Color(240, 192, 96));
        name.setAlignmentX(CENTER_ALIGNMENT);

        // Stats
        JLabel stats = new JLabel(String.format(
            "<html><center>HP: %d<br>MP: %d<br>ATK: %d<br>DEF: %d<br>SPD: %d</center></html>",
            c.getMaxHp(), c.getMaxMana(),
            c.getAttackPower(), c.getDefensePower(), c.getSpeed()));
        stats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stats.setForeground(new Color(160, 160, 200));
        stats.setAlignmentX(CENTER_ALIGNMENT);

        // Description / passive
        JLabel desc = new JLabel(
            "<html><center><i>" + c.getClassDescription()
                .replace("\n", "<br>") + "</i></center></html>");
        desc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        desc.setForeground(new Color(120, 120, 160));
        desc.setAlignmentX(CENTER_ALIGNMENT);

        card.add(name);
        card.add(Box.createVerticalStrut(10));
        card.add(stats);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);

        // Click to select / deselect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selected.contains(c)) {
                    selected.remove(c);
                    card.setBackground(COLOR_UNSELECTED);
                } else if (selected.size() < 2) {
                    selected.add(c);
                    card.setBackground(COLOR_SELECTED);
                } else {
                    JOptionPane.showMessageDialog(frame,
                        "You can only pick 2 heroes. Deselect one first.",
                        "Max 2", JOptionPane.INFORMATION_MESSAGE);
                }
                card.repaint();
            }
        });

        return card;
    }
}
