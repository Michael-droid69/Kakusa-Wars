package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveManager {

    private static final String SAVE_PATH   = "saves/save_data.txt";
    private static final String PLAYER_LOG  = "players.txt";

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
            String cls  = data.getOrDefault("party" + i + ".class", "Arthur");
            String name = data.getOrDefault("party" + i + ".name",  "Hero");
            int    hp   = Integer.parseInt(data.getOrDefault("party" + i + ".hp",   "100"));
            int    mana = Integer.parseInt(data.getOrDefault("party" + i + ".mana", "50"));

            core.Character c = switch (cls) {
                case "Arthur"  -> new characters.Arthur(name);
                case "Mage"    -> new characters.Fabby(name);
                case "Archer"  -> new characters.Tapanh(name);
                case "Rogue"   -> new characters.Van(name);
                case "Paladin" -> new characters.Star(name);
                default        -> new characters.Arthur(name);
            };

            // Force HP and mana to saved values
            // (constructor sets them to max, so we need to override)
            int hpDiff = c.getMaxHp() - hp;
            if (hpDiff > 0) c.takeDamage(hpDiff);
            int manaDiff = c.getMaxMana() - mana;
            if (manaDiff > 0) c.spendManaUnsafe(manaDiff);

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
