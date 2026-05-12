package core;

import java.util.*;

public class BattleEngine {

    // ─────────────────────────────────────────────────
    // COMBAT CONSTANTS — tweak these to balance the game
    // ─────────────────────────────────────────────────
    public static final double MISS_CHANCE    = 0.10;  // 10% basic attack miss
    public static final double CRIT_CHANCE    = 0.15;  // 15% crit
    public static final double CRIT_MULTI     = 1.80;  // crits deal 180% damage
    public static final double FLEE_SUCCESS   = 0.40;  // 40% chance to escape
    public static final double FLEE_FAIL_DMG  = 0.15;  // on fail: party takes 15% max HP
    public static final double TAUNT_LAND     = 0.60;  // 60% taunt lands
    public static final double TAUNT_MISS     = 0.70;  // 70% miss chance while taunted

    private static final Random RNG = new Random();

    // ─────────────────────────────────────────────────
    // ATTACK RESULT — returned to BattleScreen so it
    // knows what message to show and what animation to play
    // ─────────────────────────────────────────────────
    public static class AttackResult {
        public final int    damage;
        public final String type;    // "HIT" | "MISS" | "CRIT"
        public final String message; // ready-to-display battle log line

        public AttackResult(int damage, String type, String message) {
            this.damage  = damage;
            this.type    = type;
            this.message = message;
        }
    }

    // ─────────────────────────────────────────────────
    // PLAYER BASIC ATTACK
    // Handles miss / hit / crit. Also checks taunt.
    // ─────────────────────────────────────────────────
    public static AttackResult playerAttack(Character attacker, Character target) {
        // If taunted this turn, high miss chance
        double missThreshold = attacker.isTaunted() ? TAUNT_MISS : MISS_CHANCE;

        double roll = RNG.nextDouble();

        if (roll < missThreshold) {
            return new AttackResult(0, "MISS",
                attacker.getName() + " missed!");
        }

        int base = attacker.getAttackPower();

        if (roll > (1.0 - CRIT_CHANCE)) {
            int crit = (int)(base * CRIT_MULTI);
            target.takeDamage(crit);
            return new AttackResult(crit, "CRIT",
                "⚡ CRIT! " + attacker.getName() + " dealt " + crit + " to " + target.getName() + "!");
        }

        target.takeDamage(base);
        return new AttackResult(base, "HIT",
            attacker.getName() + " attacked " + target.getName() + " for " + base + " damage.");
    }

    // ─────────────────────────────────────────────────
    // PLAYER SKILL USE — single target
    // ─────────────────────────────────────────────────
    public static AttackResult playerSkill(Character attacker,
                                            int skillIndex,
                                            Character target) {
        Skill skill = attacker.getSkill(skillIndex);
        int result  = attacker.useSkill(skillIndex, target);

        if (result == -1) {
            // useSkill returns -1 when not enough mana
            return new AttackResult(0, "NO_MANA",
                attacker.getName() + " doesn't have enough mana for " + skill.getName() + "!");
        }

        if (skill.getType().contains("buff")) {
            return new AttackResult(0, "BUFF",
                attacker.getName() + " used " + skill.getName() + "!");
        }

        return new AttackResult(result, "SKILL",
            "✨ " + attacker.getName() + " used " + skill.getName()
            + " on " + target.getName() + " for " + result + " damage!");
    }

    // ─────────────────────────────────────────────────
    // PLAYER SKILL USE — AoE (hits ALL enemies)
    // BattleScreen calls this when skill type is "damage_all"
    // ─────────────────────────────────────────────────
    public static List<AttackResult> playerSkillAoe(Character attacker,
                                                      int skillIndex,
                                                      List<Character> enemies) {
        Skill skill = attacker.getSkill(skillIndex);
        List<AttackResult> results = new ArrayList<>();

        // Check mana first — spend it once
        try {
            attacker.spendMana(skill.getManaCost());
        } catch (exceptions.NotEnoughManaException e) {
            results.add(new AttackResult(0, "NO_MANA", e.getMessage()));
            return results;
        }

        int perEnemyDamage = (int)(attacker.getAttackPower() * skill.getDamageMultiplier());

        for (Character enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.takeDamage(perEnemyDamage);
                results.add(new AttackResult(perEnemyDamage, "SKILL_AOE",
                    attacker.getName() + " hit " + enemy.getName() + " for " + perEnemyDamage + "!"));
            }
        }
        return results;
    }

    // ─────────────────────────────────────────────────
    // USE ITEM
    // ─────────────────────────────────────────────────
    public static String useItem(List<Item> inventory, int itemIndex, Character target)
            throws exceptions.EmptyInventoryException {
        if (inventory.isEmpty())
            throw new exceptions.EmptyInventoryException(
                "Your bag is empty! No items left.");
        if (itemIndex < 0 || itemIndex >= inventory.size())
            throw new exceptions.EmptyInventoryException(
                "Invalid item slot.");

        Item item = inventory.remove(itemIndex); // removes after use
        return item.applyTo(target);
    }

    // ─────────────────────────────────────────────────
    // FLEE ATTEMPT
    // ─────────────────────────────────────────────────
    public static boolean attemptFlee(List<Character> party) {
        if (RNG.nextDouble() < FLEE_SUCCESS) return true;

        // Failed flee — whole party takes 15% max HP damage
        for (Character c : party) {
            if (c.isAlive()) {
                int penalty = (int)(c.getMaxHp() * FLEE_FAIL_DMG);
                c.takeDamage(penalty);
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────
    // ENEMY AI TURN
    // Returns a message string for the battle log.
    // If the return starts with "TAUNT:" BattleScreen reads
    // the target name after the colon and sets their taunt flag.
    // ─────────────────────────────────────────────────
    public static String enemyTurn(Character enemy, List<Character> party) {
        List<Character> alive = party.stream()
                                     .filter(Character::isAlive)
                                     .toList();
        if (alive.isEmpty()) return "";

        int roll = RNG.nextInt(10); // 0–9

        if (roll < 5) {
            // 50% — basic attack on random party member
            Character target = alive.get(RNG.nextInt(alive.size()));
            int dmg = enemy.attack(target);
            if (dmg == 0)
                return enemy.getName() + " attacked " + target.getName() + " but missed!";
            return enemy.getName() + " attacked " + target.getName() + " for " + dmg + " damage!";

        } else if (roll < 7) {
            // 20% — buff self (+15% ATK)
            enemy.setAttackPower((int)(enemy.getAttackPower() * 1.15));
            return "⬆ " + enemy.getName() + " powered up! ATK increased!";

        } else {
            // 30% — taunt attempt on random ally
            Character target = alive.get(RNG.nextInt(alive.size()));
            if (RNG.nextDouble() < TAUNT_LAND) {
                target.setTaunted(true);
                // Prefix "TAUNT:" tells BattleScreen to handle the flag and animation
                return "TAUNT:" + target.getName() + ":" +
                       enemy.getName() + " taunted " + target.getName() +
                       "! They must attack next turn with 70% miss chance!";
            }
            return enemy.getName() + " tried to taunt but failed!";
        }
    }

    // ─────────────────────────────────────────────────
    // END OF TURN — mana regen + clear taunt
    // Call this after every complete round (all players + all enemies acted)
    // ─────────────────────────────────────────────────
    public static void endOfTurn(List<Character> party) {
        for (Character c : party) {
            if (c.isAlive()) {
                c.regenManaPerTurn(); // +10 MP, Paladin also heals +5 HP here
                c.setTaunted(false);  // taunt only lasts 1 turn
            }
        }
    }

    // ─────────────────────────────────────────────────
    // CHECK WIN / LOSE CONDITIONS
    // ─────────────────────────────────────────────────
    public static boolean isPartyDefeated(List<Character> party) {
        return party.stream().noneMatch(Character::isAlive);
    }

    public static boolean isEnemyWaveDefeated(List<Character> enemies) {
        return enemies.stream().noneMatch(Character::isAlive);
    }

    // ─────────────────────────────────────────────────
    // WAVE SPAWNER — returns the enemy list for a given area + wave
    // area: "Forest" | "Dungeon" | "Volcano"
    // wave: 1 | 2 | 3 | 4   (wave 4 is always the boss)
    // ─────────────────────────────────────────────────
    public static List<Character> spawnWave(String area, int wave) {
        List<Character> enemies = new ArrayList<>();

        if (wave == 4) {
            // BOSS wave — one boss per area
            enemies.add(switch (area) {
                case "Forest"  -> new enemies.Bat();
                case "Dungeon" -> new enemies.Grim();
                case "Volcano" -> new enemies.Horse();
                default        -> new enemies.Fish();
            });
            return enemies;
        }

        // Waves 1–3: random mix from the area's pool
        // More enemies per wave as wave increases
        int count = wave + 1; // wave 1 = 2 enemies, wave 2 = 3, wave 3 = 4

        for (int i = 0; i < count; i++) {
            enemies.add(switch (area) {
                case "Forest"  -> RNG.nextBoolean()
                                  ? new enemies.Horse()
                                  : new enemies.Kapre();
                case "Dungeon" -> RNG.nextBoolean()
                                  ? new enemies.Bat()
                                  : new enemies.Witch();
                case "Volcano" -> new enemies.Fish(); // hard area: always titans
                default        -> new enemies.Grim();
            });
        }
        return enemies;
    }

    // ─────────────────────────────────────────────────
    // STARTING INVENTORY — call this once when a new game starts
    // ─────────────────────────────────────────────────
    public static List<Item> buildStartingInventory() {
        List<Item> inv = new ArrayList<>();
        inv.add(new Item("Health Potion",  "Restores 80 HP",          "heal",     80,  50));
        inv.add(new Item("Mana Elixir",    "Restores 60 MP",          "mana",     60,  40));
        inv.add(new Item("Revive Scroll",  "Revives a fallen ally",   "revive",   50, 150));
        return inv;
    }
}