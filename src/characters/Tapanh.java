package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Tapanh extends Character {

    private static final Random RNG = new Random();
    private double counterChance = 0.30; // 30% counter passive

    public Tapanh(String name) {
        // Fastest character: lower HP, very high SPD, solid ATK
        super(name, 190, 110, 40, 45, "assets/sprites/tapanh/");

        addSkill(new Skill("Head On Combat", "Rushes straight into target, 100% ATK",       25, 1.0,  "damage"));
        addSkill(new Skill("Pause Punch",    "Gathers power, unleashes devastating punch, 200% ATK", 50, 2.0, "damage"));
        addSkill(new Skill("Bull Slam",      "Transforms into bull, ground slam hits all, 150% ATK", 60, 1.5, "damage"));
        applyPassive();
    }

    @Override
    public void applyPassive() {
        // Reflex: +30% counter chance stored as a field; BattleEngine should call getCounterChance()
        // Also boost speed stat by 20%
        setSpeed((int)(getSpeed() * 1.2));
    }

    public double getCounterChance() { return counterChance; }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        if (roll < 0.07) return 0;               // 7% miss (very fast, rarely misses)
        if (roll > 0.88) base = (int)(base * 1.9); // 12% crit (quick reflexes)
        target.takeDamage(base);
        return base;
    }

    @Override
    public int useSkill(int skillIndex, Character target) {
        Skill skill = getSkill(skillIndex);
        try { spendMana(skill.getManaCost()); }
        catch (exceptions.NotEnoughManaException e) {
            System.out.println(e.getMessage()); return 0;
        }
        return switch (skillIndex) {
            case 0 -> { // Head On Combat
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 1 -> { // Pause Punch — single target big hit
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 2 -> { // Bull Slam — caller handles multi-target
                yield (int)(getAttackPower() * skill.getDamageMultiplier());
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Fighter — Fastest character. High counter & speed.\nPassive: Reflex (+30% counter chance, +20% SPD)";
    }
}