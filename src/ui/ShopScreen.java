package ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShopScreen extends JPanel {

    // All items the shop sells
    private static final List<core.Item> SHOP_ITEMS = List.of(
        new core.Item("Health Potion",  "Restores 80 HP",         "heal",     80,  50),
        new core.Item("Mana Elixir",    "Restores 60 MP",         "mana",     60,  40),
        new core.Item("Revive Scroll",  "Revives a fallen ally",  "revive",   50, 150),
        new core.Item("Elixir of Power","ATK +15 permanently",    "atk_buff", 15, 100),
        new core.Item("Iron Tonic",     "DEF +10 permanently",    "def_buff", 10,  80)
    );

    private JLabel goldLabel;

    public ShopScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(14, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // ── Header ──
        JLabel header = new JLabel("⚗  Shop", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.setForeground(new Color(240, 192, 96));

        goldLabel = new JLabel("Gold: " + frame.getGold() + "g", SwingConstants.CENTER);
        goldLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        goldLabel.setForeground(new Color(220, 180, 60));

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBackground(new Color(14, 10, 5));
        topPanel.add(header);
        topPanel.add(goldLabel);
        add(topPanel, BorderLayout.NORTH);

        // ── Item grid ──
        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 10));
        grid.setBackground(new Color(14, 10, 5));

        for (core.Item item : SHOP_ITEMS) {
            grid.add(buildItemRow(item, frame));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(14, 10, 5));
        add(scroll, BorderLayout.CENTER);

        // ── Continue button ──
        JButton continueBtn = new JButton("Continue to Wave " + frame.getCurrentWave() + "  ▶");
        continueBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        continueBtn.setBackground(new Color(50, 25, 90));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFocusPainted(false);
        continueBtn.setBorderPainted(false);
        continueBtn.setPreferredSize(new Dimension(280, 44));
        continueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        continueBtn.addActionListener(e -> frame.goToBattle());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(14, 10, 5));
        bottom.add(continueBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildItemRow(core.Item item, MainFrame frame) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setBackground(new Color(22, 16, 10));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 45, 20), 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel info = new JLabel("<html><b>" + item.getName() + "</b>  —  "
            + item.getDescription() + "</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(new Color(200, 190, 160));

        JButton buyBtn = new JButton("Buy  " + item.getShopCost() + "g");
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buyBtn.setBackground(new Color(60, 45, 10));
        buyBtn.setForeground(new Color(240, 200, 80));
        buyBtn.setFocusPainted(false);
        buyBtn.setBorderPainted(false);
        buyBtn.setPreferredSize(new Dimension(100, 32));

        buyBtn.addActionListener(e -> {
            if (frame.getGold() < item.getShopCost()) {
                JOptionPane.showMessageDialog(frame,
                    "Not enough gold! You need " + item.getShopCost() + "g.",
                    "Can't Afford", JOptionPane.WARNING_MESSAGE);
                return;
            }
            frame.spendGold(item.getShopCost());
            // Add a fresh copy of the item to inventory
            frame.getInventory().add(new core.Item(
                item.getName(), item.getDescription(),
                item.getEffectType(), item.getEffectValue(), item.getShopCost()));
            goldLabel.setText("Gold: " + frame.getGold() + "g");
            JOptionPane.showMessageDialog(frame,
                item.getName() + " added to your bag!",
                "Purchased", JOptionPane.INFORMATION_MESSAGE);
        });

        row.add(info,   BorderLayout.CENTER);
        row.add(buyBtn, BorderLayout.EAST);
        return row;
    }
}