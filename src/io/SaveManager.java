package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveManager {

    private static final String SAVE_PATH   = "saves/save_data.txt";
    private static final String PLAYER_LOG  = "players.txt";

    private static final String SAVE_FILE    = "saves/save_data.txt"; // ← active session
    private static final String PLAYERS_FILE = "players.txt";         // ← all-time records

/**
 * Returns true if save_data.txt exists and contains a "username=" line.
 * Used by MainMenuScreen to decide whether Continue is enabled.
 */
public static boolean hasSaveData() {
    java.io.File f = new java.io.File(SAVE_FILE);
    if (!f.exists()) return false;
    try (java.util.Scanner sc = new java.util.Scanner(f)) {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.startsWith("username=") && line.length() > "username=".length())
                return true;   // found a non-empty username line
        }
    } catch (java.io.IOException ignored) {}
    return false;
}

/**
 * Deletes save_data.txt (or empties it).
 * Called by "New Game" so a stale save can't be loaded by accident.
 */
public static void clearSaveData() {
    java.io.File f = new java.io.File(SAVE_FILE);
    if (f.exists()) f.delete();
}

/**
 * Appends or updates a player entry in players.txt.
 * Call this when:
 *   (a) a brand-new username is confirmed — pass wave=1, kills=0, turns=0
 *   (b) a session ends (win or lose) — pass their final stats
 *
 * If the username already exists, the line is updated (higher wave wins).
 * If it's new, a line is appended.
 */
public static void registerOrUpdatePlayer(String username, int highestWave,
                                           int totalKills, int totalTurns,
                                           String lastArea) throws java.io.IOException {
    java.io.File f = new java.io.File(PLAYERS_FILE);
    java.util.List<String> lines = new java.util.ArrayList<>();
    boolean found = false;

    if (f.exists()) {
        try (java.util.Scanner sc = new java.util.Scanner(f)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    lines.add(line); continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equalsIgnoreCase(username)) {
                    // Update: keep the HIGHER wave
                    int existingWave = parts.length >= 2
                        ? Integer.parseInt(parts[1].trim()) : 0;
                    int bestWave = Math.max(existingWave, highestWave);
                    lines.add(username + "," + bestWave + "," + totalKills
                              + "," + totalTurns + "," + lastArea);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        }
    }

    if (!found) {
        // Brand new player — append
        lines.add(username + "," + highestWave + "," + totalKills
                  + "," + totalTurns + "," + lastArea);
    }

    try (java.io.PrintWriter pw = new java.io.PrintWriter(
            new java.io.FileWriter(f, false))) {   // false = overwrite
        for (String l : lines) pw.println(l);
    }
}

/**
 * Reads players.txt and returns a list of String arrays.
 * Each array: [username, highestWave, totalKills, totalTurns, lastArea]
 * Used by LeaderboardScreen.
 * Returns empty list if file doesn't exist.
 */
public static java.util.List<String[]> loadLeaderboard() {
    java.util.List<String[]> result = new java.util.ArrayList<>();
    java.io.File f = new java.io.File(PLAYERS_FILE);
    if (!f.exists()) return result;
    try (java.util.Scanner sc = new java.util.Scanner(f)) {
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(",");
            if (parts.length >= 5) result.add(parts);
        }
    } catch (java.io.IOException ignored) {}
    // Sort by highest wave descending
    result.sort((a, b) -> {
        try { return Integer.compare(
            Integer.parseInt(b[1].trim()),
            Integer.parseInt(a[1].trim())); }
        catch (NumberFormatException e) { return 0; }
    });
    return result;
}

    // ─────────────────────────────────────────────────
    // USERNAME LOG
    // Called once on the UsernameScreen when player hits Start.
    // Appends the name to players.txt — never overwrites old entries.
    // ─────────────────────────────────────────────────
    public static void logUsername(String username) throws IOException {
        // Create saves/ directory if it doesn't exist yet
        Files.createDirectories(Path.of("saves"));
        // true = append mode
        try (FileWriter fw = new FileWriter(PLAYER_LOG, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(username);
            bw.newLine();
        }
    }

    // ─────────────────────────────────────────────────
    // SAVE GAME
    // Writes key=value lines to saves/save_data.txt.
    // Called between waves (before the shop screen).
    // ─────────────────────────────────────────────────
    public static void saveGame(String username,
                                 String area,
                                 int    wave,
                                 int    gold,
                                 List<core.Character> party,
                                 List<core.Item>      inventory,
                                 int enemiesKilled,
                                 int turnsTotal) throws IOException {
        Files.createDirectories(Path.of("saves"));

        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_PATH))) {
            // Header data
            pw.println("username="      + username);
            pw.println("area="          + area);
            pw.println("wave="          + wave);
            pw.println("gold="          + gold);
            pw.println("enemiesKilled=" + enemiesKilled);
            pw.println("turnsTotal="    + turnsTotal);

            // Party — save enough to reconstruct them
            pw.println("partySize=" + party.size());
            for (int i = 0; i < party.size(); i++) {
                core.Character c = party.get(i);
                pw.println("party" + i + ".class=" + c.getClass().getSimpleName());
                pw.println("party" + i + ".name="  + c.getName());
                pw.println("party" + i + ".hp="    + c.getHp());
                pw.println("party" + i + ".mana="  + c.getMana());
            }

            // Inventory — save item names so we can rebuild them
            pw.println("inventorySize=" + inventory.size());
            for (int i = 0; i < inventory.size(); i++) {
                pw.println("item" + i + "=" + inventory.get(i).getName());
            }
        }
    }

    // ─────────────────────────────────────────────────
    // LOAD GAME
    // Reads save_data.txt and returns a Map of key=value.
    // BattleScreen / MainFrame uses this map to rebuild state.
    // ─────────────────────────────────────────────────
    public static Map<String, String> loadGame() throws IOException {
        Map<String, String> data = new LinkedHashMap<>();
        if (!Files.exists(Path.of(SAVE_PATH))) return data;

        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("=")) continue;
                int eq = line.indexOf('=');
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                data.put(key, val);
            }
        }
        return data;
    }

    // ─────────────────────────────────────────────────
    // REBUILD PARTY FROM SAVE DATA
    // Takes the loaded Map and constructs Character objects.
    // ─────────────────────────────────────────────────
    public static List<core.Character> rebuildParty(Map<String, String> data) {
        List<core.Character> party = new ArrayList<>();
        int size = Integer.parseInt(data.getOrDefault("partySize", "0"));

        for (int i = 0; i < size; i++) {
            // class is saved as the simple class name e.g. "Arthur", "Fabby", "Mohammad"
            String cls  = data.getOrDefault("party" + i + ".class", "Arthur");
            String name = data.getOrDefault("party" + i + ".name",  "Hero");

            // Reconstruct the correct character type using the actual class name
            // HP and mana are intentionally reset to full (fresh start feel)
            // — only wave, area, gold, and inventory carry over
            core.Character c = switch (cls) {
                case "Arthur"   -> new characters.Arthur(name);
                case "Fabby"    -> new characters.Fabby(name);
                case "Mohammad" -> new characters.Mohammad(name);
                case "Star"     -> new characters.Star(name);
                case "Tapanh"   -> new characters.Tapanh(name);
                case "Van"      -> new characters.Van(name);
                default         -> new characters.Arthur(name);
            };

            // HP and mana start fresh (initStartingMana already called in constructor)
            // No override needed — character is ready to fight at full health
            party.add(c);
        }
        return party;
    }

    // ─────────────────────────────────────────────────
    // REBUILD INVENTORY FROM SAVE DATA
    // ─────────────────────────────────────────────────
    public static List<core.Item> rebuildInventory(Map<String, String> data) {
        List<core.Item> inv = new ArrayList<>();
        int size = Integer.parseInt(data.getOrDefault("inventorySize", "0"));

        for (int i = 0; i < size; i++) {
            String itemName = data.getOrDefault("item" + i, "");
            core.Item item = buildItemByName(itemName);
            if (item != null) inv.add(item);
        }
        return inv;
    }

    // Maps saved item name back to an Item object
    private static core.Item buildItemByName(String name) {
        return switch (name) {
            case "Health Potion" ->
                new core.Item("Health Potion",  "Restores 80 HP",         "heal",   80, 50);
            case "Mana Elixir" ->
                new core.Item("Mana Elixir",    "Restores 60 MP",         "mana",   60, 40);
            case "Revive Scroll" ->
                new core.Item("Revive Scroll",  "Revives a fallen ally",  "revive", 50, 150);
            case "Elixir of Power" ->
                new core.Item("Elixir of Power","ATK +15 permanently",    "atk_buff",15,100);
            case "Iron Tonic" ->
                new core.Item("Iron Tonic",     "DEF +10 permanently",    "def_buff",10, 80);
            default -> null;
        };
    }

    // ─────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────
    public static boolean hasSaveFile() {
        return Files.exists(Path.of(SAVE_PATH));
    }

    public static void deleteSave() throws IOException {
        Files.deleteIfExists(Path.of(SAVE_PATH));
    }
}
