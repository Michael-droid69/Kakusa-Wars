package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Fabby extends Character {

    private static final Random RNG = new Random();

    public Fabby(String name) {
        // Worst stats overall — frail, low ATK, decent mana pool for skills
        super(name, 140, 130, 15, 20, "assets/sprites/fabby/");

        addSkill(new Skill("Loud Cry",         "Cries so loud enemies lose 20% DEF for 2 turns", 20, 0.0, "debuff"));
        addSkill(new Skill("Cry Puddle",       "Cries a healing puddle, restores 30% max HP to self & allies", 40, 0.0, "heal"));
        addSkill(new Skill("Laser Water Eyes", "Fires tear lasers at target, 60% ATK (low dmg but real)", 30, 0.6, "damage"));
        applyPassive();
    }

    @Override
    public void applyPassive() {
        // Waterworks: tracked by BattleEngine — regen +10 mana per turn when HP < 40%
        // No stat change on init; the effect is checked at turn start
    }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        if (roll < 0.15) return 0;               // 15% miss (he's not a fighter)
        if (roll > 0.95) base = (int)(base * 1.5); // rare 5% crit
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
            case 0 -> { // Loud Cry — caller should apply -20% DEF debuff to all enemies
                yield 0;
            }
            case 1 -> { // Cry Puddle — caller handles healing all allies for 30% max HP
                yield 0;
            }
            case 2 -> { // Laser Water Eyes
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Support — Frail crybaby. Weakest stats but vital utility.\nPassive: Waterworks (regen +10 mana/turn when HP < 40%)";
    }
}