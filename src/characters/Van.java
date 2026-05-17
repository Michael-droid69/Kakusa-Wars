package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Van extends Character {

    private static final Random RNG = new Random();
    private double luckBonus = 0.20; // +20% crit/lucky event chance

    public Van(String name) {
        // Rich and lucky: moderate stats, high mana, high crit chance
        super(name, 200, 140, 35, 28, "assets/sprites/van/");

        addSkill(new Skill("Throw Gold",       "Pelts target with gold coins, 70% ATK, high crit chance", 15, 0.7, "damage"));
        addSkill(new Skill("Flying Blow Kiss", "Sends a devastating kiss projectile, stuns or drains 40% HP", 40, 0.4, "damage"));
        addSkill(new Skill("Summon Wives",     "Summons 3 warrior wives to protect Van for 3 turns (taunt)", 60, 0.0, "buff"));
        applyPassive();
        initStartingMana();
    }

    @Override
    public void applyPassive() {
        // Old Money: +25% mana pool, luck bonus stored as field
        setMaxMana((int)(getMaxMana() * 1.25));
    }

    public double getLuckBonus() { return luckBonus; }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        // Luck bonus increases crit window
        if (roll < 0.08) return 0;
        if (roll > (0.85 - luckBonus)) base = (int)(base * 2.0); // high crit rate
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
            case 0 -> { // Throw Gold — lucky crit chance
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                if (RNG.nextDouble() < 0.25) dmg = (int)(dmg * 2.0); // 25% gold crit
                target.takeDamage(dmg); yield dmg;
            }
            case 1 -> { // Flying Blow Kiss — stun or HP drain
                int dmg = (int)(target.getMaxHp() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg; // caller applies stun if RNG favors
            }
            case 2 -> { // Summon Wives — caller applies taunt/shield for 3 turns
                yield 0;
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Rich Noble — High luck & mana generation. Glass cannon with tricks.\nPassive: Old Money (+25% mana, +20% luck/crit)";
    }
}