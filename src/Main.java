/**
 * RPG Battle System
 * Authors: [Your Name] & [Partner Name]
 * OOP Final Project
 */

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater ensures the UI is built on the correct thread
        SwingUtilities.invokeLater(() -> new ui.MainFrame());
    }
}