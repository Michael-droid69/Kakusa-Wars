package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    // ── CardLayout swaps which panel is visible ──
    private final CardLayout layout = new CardLayout();
    private final JPanel     root   = new JPanel(layout);

    // ── Shared game state — all screens read/write these ──
    private String                username       = "Player";
    private List<core.Character>  party          = new ArrayList<>();
    private List<core.Item>       inventory      = new ArrayList<>();
    private String                currentArea    = "Forest";
    private int                   currentWave    = 1;
    private int                   gold           = 0;
    private int                   enemiesKilled  = 0;
    private int                   turnsTotal     = 0;

    public MainFrame() {
        setTitle("⚔  RPG Battle System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(1440, 680));
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().add(root);
        setVisible(true);

        // Start on the username screen
        goToMainMenu();
    }

    // ─────────────────────────────────────────────────
    // NAVIGATION METHODS — each screen calls one of these
    // ─────────────────────────────────────────────────

    public void goToMainMenu() {
    addScreen(new MainMenuScreen(this), "mainmenu");
    layout.show(root, "mainmenu");
}

public void goToLeaderboard() {
    addScreen(new LeaderboardScreen(this), "leaderboard");
    layout.show(root, "leaderboard");
}
    public void goToUsername() {
        addScreen(new UsernameScreen(this), "username");
        layout.show(root, "username");
    }

    public void goToCharacterSelect() {
        addScreen(new CharacterSelectScreen(this), "charselect");
        layout.show(root, "charselect");
    }

    public void goToAreaSelect() {
        addScreen(new AreaSelectScreen(this), "areaselect");
        layout.show(root, "areaselect");
    }

    public void goToBattle() {
        addScreen(new BattleScreen(this), "battle_" + currentArea + "_" + currentWave);
        layout.show(root, "battle_" + currentArea + "_" + currentWave);
    }

    public void goToShop() {
        addScreen(new ShopScreen(this), "shop_" + currentWave);
        layout.show(root, "shop_" + currentWave);
    }

    public void goToGameOver(boolean victory) {
        addScreen(new GameOverScreen(this, victory), "gameover");
        layout.show(root, "gameover");
    }

    private void addScreen(JPanel panel, String key) {
        root.add(panel, key);
        root.revalidate();
        root.repaint();
    }

    // ─────────────────────────────────────────────────
    // GETTERS — screens read state from here
    // ─────────────────────────────────────────────────
    public String               getUsername()      { return username; }
    public List<core.Character> getParty()         { return party; }
    public List<core.Item>      getInventory()     { return inventory; }
    public String               getCurrentArea()   { return currentArea; }
    public int                  getCurrentWave()   { return currentWave; }
    public int                  getGold()          { return gold; }
    public int                  getEnemiesKilled() { return enemiesKilled; }
    public int                  getTurnsTotal()    { return turnsTotal; }

    // ─────────────────────────────────────────────────
    // SETTERS — screens update state here
    // ─────────────────────────────────────────────────
    public void setUsername(String n)                    { this.username = n; }
    public void setParty(List<core.Character> p)         { this.party = p; }
    public void setInventory(List<core.Item> inv)        { this.inventory = inv; }
    public void setCurrentArea(String area)              { this.currentArea = area; }
    public void setCurrentWave(int wave)                 { this.currentWave = wave; }
    public void addGold(int amount)                      { this.gold += amount; }
    public void spendGold(int amount)                    { this.gold = Math.max(0, gold - amount); }
    public void addEnemyKill()                           { this.enemiesKilled++; }
    public void addTurn()                                { this.turnsTotal++; }
    public void nextWave()                               { this.currentWave++; }

    // ─────────────────────────────────────────────────
    // SAVE / LOAD shortcuts used by BattleScreen and UsernameScreen
    // ─────────────────────────────────────────────────
    public void saveCurrentGame() {
        try {
            io.SaveManager.saveGame(username, currentArea, currentWave,
                gold, party, inventory, enemiesKilled, turnsTotal);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this,
                "Save failed: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean loadSavedGame() {
        try {
            Map<String, String> data = io.SaveManager.loadGame();
            if (data.isEmpty()) return false;

            username      = data.getOrDefault("username", "Player");
            currentArea   = data.getOrDefault("area", "Forest");
            currentWave   = Integer.parseInt(data.getOrDefault("wave", "1"));
            gold          = Integer.parseInt(data.getOrDefault("gold", "0"));
            enemiesKilled = Integer.parseInt(data.getOrDefault("enemiesKilled", "0"));
            turnsTotal    = Integer.parseInt(data.getOrDefault("turnsTotal",    "0"));
            party         = io.SaveManager.rebuildParty(data);
            inventory     = io.SaveManager.rebuildInventory(data);
            return true;
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this,
                "Load failed: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
