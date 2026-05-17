package characters;

import core.Character;
import core.Skill;
import java.util.Random;

public class Mohammad extends Character {

    private static final Random RNG = new Random();

    public Mohammad(String name) {
        // Most firepower: highest ATK in the game, low DEF
        super(name, 210, 100, 65, 30, "assets/sprites/mohammad/");

        addSkill(new Skill("Bomb Throw",     "Hurls a bomb at single target, 120% ATK",           25, 1.2, "damage"));
        addSkill(new Skill("Cluster Bombs",  "Throws one bomb that splits into 4, hits all, 90% ATK each", 50, 3.6, "damage")); // 4 × 0.9 total represented
        addSkill(new Skill("Self Destruct",  "Charges enemy & detonates, 250% ATK to target, 60% to allies, leaves self at 50% HP", 70, 2.5, "damage"));
        applyPassive();
        initStartingMana();
    }

    @Override
    public void applyPassive() {
        // Volatility: +20% ATK, -15% DEF — high risk high reward
        setAttackPower((int)(getAttackPower() * 1.2));
        setDefensePower((int)(getDefensePower() * 0.85));
    }

    @Override
    public int attack(Character target) {
        int base = getAttackPower();
        double roll = RNG.nextDouble();
        if (roll < 0.10) return 0;               // 10% miss
        if (roll > 0.88) base = (int)(base * 2.0); // 12% massive crit (explosions)
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
            case 0 -> { // Bomb Throw
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg); yield dmg;
            }
            case 1 -> { // Cluster Bombs — caller handles all enemies (90% ATK each)
                yield (int)(getAttackPower() * 0.9); // return per-hit dmg; caller applies ×4
            }
            case 2 -> { // Self Destruct — caller: deal 250% to target, 60% ATK to all allies, set self HP to 50%
                int dmg = (int)(getAttackPower() * skill.getDamageMultiplier());
                target.takeDamage(dmg);
                // Caller must also: reduce Mohammad's HP to (getMaxHp() / 2)
                // and deal (int)(getAttackPower() * 0.6) to each ally
                yield dmg;
            }
            default -> 0;
        };
    }

    @Override
    public String getClassDescription() {
        return "Demolitions — Highest ATK in the roster. Glass cannon.\nPassive: Volatility (+20% ATK, -15% DEF)";
    }
}