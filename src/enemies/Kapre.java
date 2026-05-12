package enemies;

import core.Character;
import java.util.Random;

public class Kapre extends Character {
    private static final Random RNG = new Random();

    public Kapre() {
        super("Kapre", 80, 0, 24, 6, "assets/sprites/enemies/kapre/");
    }

    @Override
    public int attack(Character target) {
        int dmg = getAttackPower() + RNG.nextInt(10);
        target.takeDamage(dmg);
        return dmg;
    }

    @Override
    public int useSkill(int i, Character target) {
        return attack(target);
    }

    @Override
    public void applyPassive() {
        // no passive
    }

    @Override
    public String getClassDescription() {
        return "Kapre — watchful bruiser.";
    }
}
