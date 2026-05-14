package ui;

import javax.swing.*;
import java.awt.*;

public class UsernameScreen extends JPanel {

    public UsernameScreen(MainFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(12, 12, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(14, 0, 14, 0);

        // ── Title ──
        JLabel title = new JLabel("⚔  RPG Battle System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(240, 192, 96));
        g.gridy = 0; add(title, g);

        JLabel sub = new JLabel("Choose your heroes. Conquer the realm.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(140, 140, 170));
        g.gridy = 1; add(sub, g);

        // ── Name input ──
        JLabel prompt = new JLabel("Enter your username:");
        prompt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        prompt.setForeground(Color.LIGHT_GRAY);
        g.gridy = 2; add(prompt, g);

        JTextField nameField = new JTextField(22);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBackground(new Color(28, 28, 40));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 120), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        g.gridy = 3; add(nameField, g);

        // ── Start button ──
        JButton startBtn = makeButton("▶   Start New Game",
                                      new Color(70, 30, 110), Color.WHITE);
        g.gridy = 4; add(startBtn, g);

        startBtn.addActionListener(e -> {
    // 1. Get and clean the input
    String name = nameField.getText().trim();

    try {
        // 2. VALIDATION (The "Guard Clause")
        if (name.isEmpty() || name.length() > 20) {
            throw new exceptions.InvalidSelectionException(
                "Username must be 1–20 characters. You entered: \"" + name + "\""
            );
        }

        // 3. FILE OPERATIONS (The "Persistence" Layer)
        // We do this BEFORE switching screens so we don't lose data
        io.SaveManager.registerOrUpdatePlayer(name, 0, 0, 0, "Forest");
        io.SaveManager.logUsername(name); 

        // 4. STATE UPDATE (Updating the MainFrame)
        frame.setUsername(name);
        frame.setInventory(core.BattleEngine.buildStartingInventory());

        // 5. NAVIGATION (Success! Move to next screen)
        frame.goToCharacterSelect();

    } catch (exceptions.InvalidSelectionException ex) {
        // Specific UI warning for bad input
        JOptionPane.showMessageDialog(frame, ex.getMessage(),
            "Invalid Username", JOptionPane.WARNING_MESSAGE);
            
    } catch (java.io.IOException ex) {
        // Serious error if the hard drive is locked or file is missing
        JOptionPane.showMessageDialog(frame,
            "Critical Error: Could not save player data.\n" + ex.getMessage(),
            "File System Error", JOptionPane.ERROR_MESSAGE);
            
    } catch (Exception ex) {
        // General catch-all to prevent the whole game from crashing
        System.err.println("Unexpected error: " + ex.getMessage());
    }
});

        // ── Continue button — only shown if save file exists ──
        if (io.SaveManager.hasSaveFile()) {
            JButton continueBtn = makeButton("⟳   Continue Saved Game",
                                             new Color(20, 50, 30),
                                             new Color(100, 220, 150));
            g.gridy = 5; add(continueBtn, g);

            continueBtn.addActionListener(e -> {
                if (frame.loadSavedGame()) {
                    // Jump straight to battle on the saved wave
                    frame.goToBattle();
                }
            });
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
        btn.setPreferredSize(new Dimension(260, 44));
        return btn;
    }
}