package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class UsernameScreen extends JPanel {
    private Image background;

    public UsernameScreen(MainFrame frame) {
        // 1. LOAD BACKGROUND
        try {
            background = ImageIO.read(new File("assets/wallpaper/username_bg.png"));
        } catch (Exception e) {
            System.err.println("Could not load background image.");
        }

        // Use BorderLayout to anchor the card to the WEST (Left)
        setLayout(new BorderLayout());

        // 2. CREATE THE "FROSTED GLASS" CARD
        // We use an anonymous class to override the paint method for the blur effect
        JPanel frostedCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // This simulates the "Blur/Frost" look:
                // A dark, semi-transparent blue/black fill
                g2.setColor(new Color(15, 20, 35, 180)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                // The "Surroundings" effect (Blue Glow/Outer Border)
                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(0, 150, 255, 60)); // Soft blue glow
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                
                g2.dispose();
            }
        };
        frostedCard.setOpaque(false);
        frostedCard.setBorder(new EmptyBorder(40, 50, 40, 50)); // Inner padding

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.WEST; // Align items inside to the left
        g.insets = new Insets(10, 0, 10, 0);

        // ── Title ──
        JLabel title = new JLabel("⚔  RPG Battle System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(240, 192, 96));
        g.gridy = 0; frostedCard.add(title, g);

        JLabel sub = new JLabel("Choose your heroes. Conquer the realm.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(160, 160, 190));
        g.gridy = 1; frostedCard.add(sub, g);

        // ── Input Section ──
        JLabel prompt = new JLabel("Enter your username:");
        prompt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        prompt.setForeground(Color.LIGHT_GRAY);
        g.gridy = 2; g.insets = new Insets(30, 0, 5, 0); 
        frostedCard.add(prompt, g);

        JTextField nameField = new JTextField(18);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameField.setBackground(new Color(10, 10, 20));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(new Color(0, 150, 255));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255, 100), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        g.gridy = 3; g.insets = new Insets(0, 0, 20, 0);
        frostedCard.add(nameField, g);

        // ── Buttons ──
        // ── Start button ──
        JButton startBtn = makeButton("▶   Start New Game", new Color(60, 30, 140), Color.WHITE);
        g.gridy = 4; frostedCard.add(startBtn, g);

        // ADD THIS PART BACK IN:
        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            try {
                if (name.isEmpty() || name.length() > 20) {
                    throw new exceptions.InvalidSelectionException(
                        "Username must be 1–20 characters."
                    );
                }

                // Save and Navigate
                io.SaveManager.registerOrUpdatePlayer(name, 0, 0, 0, "Forest");
                io.SaveManager.logUsername(name); 
                frame.setUsername(name);
                frame.setInventory(core.BattleEngine.buildStartingInventory());
                frame.goToCharacterSelect();

            } catch (exceptions.InvalidSelectionException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Invalid Username", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        if (io.SaveManager.hasSaveFile()) {
            JButton continueBtn = makeButton("⟳   Continue Saved Game", new Color(25, 50, 40), new Color(120, 255, 180));
            g.gridy = 5; frostedCard.add(continueBtn, g);
            
            // ADD THIS BACK TOO:
            continueBtn.addActionListener(e -> {
                if (frame.loadSavedGame()) {
                    frame.goToBattle();
                }
            });
        }

        // 3. THE "NOT-CORNERED" LEFT ALIGNMENT
        // We put the card in a wrapper with a Large Left Margin (EmptyBorder)
        JPanel leftMarginWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftMarginWrapper.setOpaque(false);
        leftMarginWrapper.setBorder(new EmptyBorder(100, 80, 0, 0)); // 100 top, 80 left
        leftMarginWrapper.add(frostedCard);

        add(leftMarginWrapper, BorderLayout.WEST);
    }

    // This handles the background image painting
    // This handles the background image painting with Center-Crop logic
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            
            // 1. Get current panel and image dimensions
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = background.getWidth(this);
            int imgH = background.getHeight(this);

            // 2. Calculate the scale factor required to "COVER" the panel
            double scaleX = (double) panelW / imgW;
            double scaleY = (double) panelH / imgH;
            double scale = Math.max(scaleX, scaleY); // Math.max prevents gaps; Math.min would "fit"

            // 3. Calculate new scaled dimensions
            int newW = (int) (imgW * scale);
            int newH = (int) (imgH * scale);

            // 4. Center the scaled image (Calculating the negative offset/cut)
            int x = (panelW - newW) / 2;
            int y = (panelH - newH) / 2;

            // 5. Draw with high quality interpolation
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(background, x, y, newW, newH, this);
            
            // 6. Optional: Darken the image slightly (makes the UI "pop")
            g2.setColor(new Color(0, 0, 0, 70)); 
            g2.fillRect(0, 0, panelW, panelH);
            
            g2.dispose();
        }
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(280, 48));
        return btn;
    }
}