package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Arthur extends Character {

    private static final Random RNG = new Random();
    private boolean berserkActive = false;

    public Arthur(String name) {
        super(name, 250, 100, 50, 25, "assets/sprites/arthur/");

        addSkill(new Skill("Slash",        "Deals a swift sword strike, 80% ATK",          20, 0.8,  "damage"));
        addSkill(new Skill("Multi Slash",  "Rapid 3-hit combo, each hit 50% ATK",           35, 1.5,  "damage")); // 3 × 0.5 = 150% total
        addSkill(new Skill("Ground Slash", "Slams blade into earth, shockwave hits all, 130% ATK", 55, 1.3, "damage"));
        applyPassive();
        initStartingMana();
    }

    @Override
    public void applyPassive() {
        // Iron Skin: +20% defense on init
        setDefensePower((int)(getDefensePower() * 1.2));
    }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        if (roll < 0.10) return 0;               // 10% miss
        if (roll > 0.90) base = (int)(base * 1.8); // 10% crit
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
            case 0 -> { // Slash
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 1 -> { // Multi Slash — 3 hits, caller can call 3× or treat as 150% total
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 2 -> { // Ground Slash — caller handles multi-target
                yield (int)(getAttackPower() * skill.getDamageMultiplier());
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Warrior — Tanky frontliner. High HP and DEF.\nPassive: Iron Skin (+20% DEF)";
    }
}