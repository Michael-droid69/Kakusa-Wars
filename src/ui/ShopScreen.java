package ui;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private BufferedImage backgroundImage;

    public ShopScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        
        // Stop battle music and play shop music
        io.MusicManager.playShopMusic();
        
        // Play merchant speech (once, no loop)
        io.MusicManager.playSoundEffect("speech.wav");
        
        // Load background image
        loadBackgroundImage();

        // ── RIGHT SIDE: Shop Items Panel ──
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 40));
        rightPanel.setPreferredSize(new Dimension(450, 0)); // Fixed width for right panel

        // Header with gold
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel shopTitle = new JLabel("⚗ Merchant's Wares");
        shopTitle.setFont(new Font("Serif", Font.BOLD, 26));
        shopTitle.setForeground(new Color(255, 235, 180));
        shopTitle.setAlignmentX(CENTER_ALIGNMENT);
        
        goldLabel = new JLabel("💰 Gold: " + frame.getGold() + "g");
        goldLabel.setFont(new Font("Serif", Font.BOLD, 20));
        goldLabel.setForeground(new Color(255, 215, 100));
        goldLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(shopTitle);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(goldLabel);

        // ── Scrollable Item List ──
        JPanel itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setOpaque(false);

        for (core.Item item : SHOP_ITEMS) {
            itemsContainer.add(buildItemCard(item, frame));
            itemsContainer.add(Box.createVerticalStrut(12));
        }

        JScrollPane scrollPane = new JScrollPane(itemsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 60, 30, 180), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.setBackground(new Color(0, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // ── Continue Button ──
        JButton continueBtn = new JButton("Continue to Wave " + frame.getCurrentWave() + "  ▶");
        continueBtn.setFont(new Font("Serif", Font.BOLD, 16));
        continueBtn.setBackground(new Color(60, 40, 80, 220));
        continueBtn.setForeground(new Color(255, 240, 200));
        continueBtn.setFocusPainted(false);
        continueBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 140), 2),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        continueBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        continueBtn.setAlignmentX(CENTER_ALIGNMENT);
        continueBtn.addActionListener(e -> frame.goToBattle());
        
        // Hover effect
        continueBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                continueBtn.setBackground(new Color(80, 60, 100, 240));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                continueBtn.setBackground(new Color(60, 40, 80, 220));
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        buttonPanel.add(continueBtn);

        rightPanel.add(headerPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add right panel to the east side
        add(rightPanel, BorderLayout.EAST);
    }

    private void loadBackgroundImage() {
        try {
            File bgFile = new File("assets/wallpaper/shop_sell.png");
            if (bgFile.exists()) {
                backgroundImage = ImageIO.read(bgFile);
            }
        } catch (IOException e) {
            System.out.println("Could not load shop background: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                
                if (panelWidth > 0 && panelHeight > 0) {
                    int imgWidth = backgroundImage.getWidth();
                    int imgHeight = backgroundImage.getHeight();
                    
                    // Scale to cover the entire panel
                    double scale = Math.max((double) panelWidth / imgWidth, 
                                          (double) panelHeight / imgHeight);
                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);
                    
                    // Center the image
                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;
                    
                    g2.drawImage(backgroundImage, x, y, scaledWidth, scaledHeight, null);
                }
            } finally {
                g2.dispose();
            }
        } else {
            // Fallback gradient background
            Graphics2D g2 = (Graphics2D) g.create();
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 15, 10),
                0, getHeight(), new Color(40, 30, 20)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private JPanel buildItemCard(core.Item item, MainFrame frame) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(12, 0));
        card.setBackground(new Color(25, 20, 15, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 50, 200), 2),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(400, 90));

        // ── Left: Item Info ──
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(255, 235, 180));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(item.getDescription());
        descLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        descLabel.setForeground(new Color(200, 185, 150));
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(descLabel);

        // ── Right: Buy Button ──
        JButton buyBtn = new JButton("Buy " + item.getShopCost() + "g");
        buyBtn.setFont(new Font("Serif", Font.BOLD, 14));
        buyBtn.setBackground(new Color(80, 60, 20, 220));
        buyBtn.setForeground(new Color(255, 220, 120));
        buyBtn.setFocusPainted(false);
        buyBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(140, 110, 50), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        buyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        buyBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                buyBtn.setBackground(new Color(100, 80, 30, 240));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                buyBtn.setBackground(new Color(80, 60, 20, 220));
            }
        });

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
            goldLabel.setText("💰 Gold: " + frame.getGold() + "g");
            
            // Success feedback
            JOptionPane.showMessageDialog(frame,
                "✓ " + item.getName() + " added to your bag!",
                "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buyBtn, BorderLayout.EAST);
        
        return card;
    }
}
