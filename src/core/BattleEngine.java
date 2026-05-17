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
    // Logs the ACTUAL damage dealt (after DEF reduction) so the
    // battle log always matches the HP bar change.
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
        int hpBefore = target.getHp();

        if (roll > (1.0 - CRIT_CHANCE)) {
            int raw  = (int)(base * CRIT_MULTI);
            target.takeDamage(raw);
            int actual = hpBefore - target.getHp();   // real HP lost after DEF
            return new AttackResult(actual, "CRIT",
                "⚡ CRIT! " + attacker.getName() + " dealt " + actual + " to " + target.getName() + "!");
        }

        target.takeDamage(base);
        int actual = hpBefore - target.getHp();        // real HP lost after DEF
        return new AttackResult(actual, "HIT",
            attacker.getName() + " attacked " + target.getName() + " for " + actual + " damage.");
    }

    // ─────────────────────────────────────────────────
    // PLAYER SKILL USE — single target
    // Captures HP before/after so the log shows actual damage dealt.
    // ─────────────────────────────────────────────────
    public static AttackResult playerSkill(Character attacker,
                                            int skillIndex,
                                            Character target) {
        Skill skill = attacker.getSkill(skillIndex);

        if (skill.getType().contains("buff") || skill.getType().equals("heal") || skill.getType().equals("debuff")) {
            int result = attacker.useSkill(skillIndex, target);
            return new AttackResult(0, "BUFF",
                attacker.getName() + " used " + skill.getName() + "!");
        }

        int hpBefore = target.getHp();
        int result   = attacker.useSkill(skillIndex, target);

        if (result == -1) {
            return new AttackResult(0, "NO_MANA",
                attacker.getName() + " doesn't have enough mana for " + skill.getName() + "!");
        }

        int actual = hpBefore - target.getHp();   // real HP lost after DEF
        return new AttackResult(actual, "SKILL",
            "✨ " + attacker.getName() + " used " + skill.getName()
            + " on " + target.getName() + " for " + actual + " damage!");
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
                int hpBefore = enemy.getHp();
                enemy.takeDamage(perEnemyDamage);
                int actual = hpBefore - enemy.getHp();
                results.add(new AttackResult(actual, "SKILL_AOE",
                    attacker.getName() + " hit " + enemy.getName() + " for " + actual + "!"));
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
    //
    // Enemies now:
    //  • Always attack a RANDOM living party member (not just one)
    //  • Deal meaningful damage scaled by wave number
    //  • Bosses (wave 4) have heavy crit chance
    // ─────────────────────────────────────────────────
    public static String enemyTurn(Character enemy, List<Character> party) {
        return enemyTurn(enemy, party, 1);
    }

    public static String enemyTurn(Character enemy, List<Character> party, int wave) {
        List<Character> alive = party.stream()
                                     .filter(Character::isAlive)
                                     .toList();
        if (alive.isEmpty()) return "";

        // Wave scaling multiplier: each wave enemies hit harder
        // wave 1 = 1.0×, wave 2 = 1.3×, wave 3 = 1.6×, wave 4 (boss) = 2.2×
        double waveScale = switch (wave) {
            case 2  -> 1.30;
            case 3  -> 1.60;
            case 4  -> 2.20;
            default -> 1.00;
        };

        int roll = RNG.nextInt(10); // 0–9

        if (roll < 6) {
            // 60% — basic attack on a RANDOM living party member
            Character target = alive.get(RNG.nextInt(alive.size()));
            int hpBefore = target.getHp();

            // Boss wave: 25% crit chance with 2× multiplier
            boolean isBossCrit = (wave == 4) && (RNG.nextDouble() < 0.25);
            int rawDmg = enemy.attack(target);   // enemy.attack already calls takeDamage
            // enemy.attack already applied damage; we need to undo and redo with scaling
            // Instead, compute scaled damage directly and apply it
            // Re-heal the damage enemy.attack() already dealt, then apply scaled version
            target.heal(hpBefore - target.getHp()); // undo the attack
            target.setAlive(hpBefore > 0);           // restore alive state if it was killed

            int scaledRaw = (int)(enemy.getAttackPower() * waveScale);
            if (isBossCrit) scaledRaw = (int)(scaledRaw * 2.0);
            // Add some variance (+0 to +30% of base)
            scaledRaw += RNG.nextInt(Math.max(1, (int)(enemy.getAttackPower() * 0.30)));

            int hpBefore2 = target.getHp();
            target.takeDamage(scaledRaw);
            int actual = hpBefore2 - target.getHp();

            if (actual == 0 && scaledRaw > 0) actual = 1; // always show at least 1

            String prefix = isBossCrit ? "💥 BOSS CRIT! " : "";
            return prefix + enemy.getName() + " attacked " + target.getName()
                   + " for " + actual + " damage!";

        } else if (roll < 8) {
            // 20% — buff self (+15% ATK, scales with wave)
            double buffAmt = 1.0 + (0.10 * wave); // wave1=+10%, wave4=+40%
            enemy.setAttackPower((int)(enemy.getAttackPower() * buffAmt));
            return "⬆ " + enemy.getName() + " powered up! ATK increased!";

        } else {
            // 20% — taunt attempt on random ally
            Character target = alive.get(RNG.nextInt(alive.size()));
            if (RNG.nextDouble() < TAUNT_LAND) {
                target.setTaunted(true);
                return "TAUNT:" + target.getName() + ":" +
                       enemy.getName() + " taunted " + target.getName() +
                       "! They must attack next turn with 70% miss chance!";
            }
            // Taunt failed — still deal a light hit to a random target
            Character hitTarget = alive.get(RNG.nextInt(alive.size()));
            int hpBefore = hitTarget.getHp();
            int rawDmg = (int)(enemy.getAttackPower() * waveScale * 0.6);
            hitTarget.takeDamage(rawDmg);
            int actual = hpBefore - hitTarget.getHp();
            return enemy.getName() + " tried to taunt but failed! Lashed out at "
                   + hitTarget.getName() + " for " + actual + " damage!";
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
    // Enemies scale in stats each wave so later waves are harder.
    // ─────────────────────────────────────────────────
    public static List<Character> spawnWave(String area, int wave) {
        List<Character> enemies = new ArrayList<>();

        if (wave == 4) {
            // BOSS wave — one powerful boss per area, heavily buffed
            Character boss = switch (area) {
                case "Forest"  -> new enemies.Bat();
                case "Dungeon" -> new enemies.Grim();
                case "Volcano" -> new enemies.Horse();
                default        -> new enemies.Fish();
            };
            // Boss gets massive stat boosts
            boss.setMaxHp(boss.getMaxHp() * 5);
            boss.setAttackPower((int)(boss.getAttackPower() * 4.0));
            boss.setDefensePower((int)(boss.getDefensePower() * 2.5));
            // Heal to new max HP
            boss.heal(boss.getMaxHp());
            enemies.add(boss);
            return enemies;
        }

        // Waves 1–3: random mix from the area's pool, count grows with wave
        // wave 1 = 2 enemies, wave 2 = 3, wave 3 = 4
        int count = wave + 1;

        // Stat scale per wave: wave1=1.0, wave2=1.4, wave3=1.9
        double statScale = switch (wave) {
            case 2  -> 1.40;
            case 3  -> 1.90;
            default -> 1.00;
        };

        for (int i = 0; i < count; i++) {
            Character e = switch (area) {
                case "Forest"  -> RNG.nextBoolean()
                                  ? new enemies.Horse()
                                  : new enemies.Kapre();
                case "Dungeon" -> RNG.nextBoolean()
                                  ? new enemies.Bat()
                                  : new enemies.Witch();
                case "Volcano" -> new enemies.Fish();
                default        -> new enemies.Grim();
            };

            // Apply wave scaling to stats
            if (wave > 1) {
                e.setMaxHp((int)(e.getMaxHp() * statScale));
                e.setAttackPower((int)(e.getAttackPower() * statScale));
                e.setDefensePower((int)(e.getDefensePower() * statScale));
                e.heal(e.getMaxHp()); // heal to new max
            }

            enemies.add(e);
        }
        return enemies;
    }

    // ─────────────────────────────────────────────────
    // GOLD LOOT — call when an enemy is defeated
    // Returns a random gold amount based on enemy ATK/HP.
    // Never too much — capped at 80g per enemy, boss up to 200g.
    // ─────────────────────────────────────────────────
    public static int rollGoldDrop(Character enemy, int wave) {
        // Base gold: small range tied to enemy strength
        int base = 5 + (enemy.getAttackPower() / 4) + (enemy.getMaxHp() / 20);
        // Wave bonus: later waves drop a bit more
        base += (wave - 1) * 5;
        // Random variance ±40%
        int variance = Math.max(1, (int)(base * 0.40));
        int gold = base + RNG.nextInt(variance * 2 + 1) - variance;
        // Cap: regular enemies max 80g, boss (wave 4) max 200g
        int cap = (wave == 4) ? 200 : 80;
        return Math.max(1, Math.min(gold, cap));
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