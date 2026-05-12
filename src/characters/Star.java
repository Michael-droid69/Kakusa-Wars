package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Star extends Character {

    private static final Random RNG = new Random();

    public Star(String name) {
        // Tougher and more durable than Arthur; HP and DEF are higher, ATK is similar
        super(name, 300, 90, 45, 20, "assets/sprites/star/");

        addSkill(new Skill("Axe Attack",        "Heavy axe blow, 90% ATK",                    20, 0.9, "damage"));
        addSkill(new Skill("Spin Swing",         "Spins axe in wide arc, hits all enemies, 110% ATK", 40, 1.1, "damage"));
        addSkill(new Skill("Dragon Fire Breath", "Transforms and breathes fire, 180% ATK to all", 65, 1.8, "damage"));
        applyPassive();
    }

    @Override
    public void applyPassive() {
        // Dragon Hide: +25% DEF, +15% HP
        setDefensePower((int)(getDefensePower() * 1.25));
        setMaxHp((int)(getMaxHp() * 1.15));
    }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        if (roll < 0.08) return 0;               // 8% miss (slightly more accurate than Arthur)
        if (roll > 0.92) base = (int)(base * 1.7); // 8% crit
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
            case 0 -> { // Axe Attack
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 1 -> { // Spin Swing — caller handles multi-target
                yield (int)(getAttackPower() * skill.getDamageMultiplier());
            }
            case 2 -> { // Dragon Fire Breath — caller handles multi-target
                yield (int)(getAttackPower() * skill.getDamageMultiplier());
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Half-Dragon Warrior — Maximum durability. High HP and DEF.\nPassive: Dragon Hide (+25% DEF, +15% HP)";
    }
}