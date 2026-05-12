package enemies;

import core.Character;
import java.util.Random;

public class Horse extends Character {
    private static final Random RNG = new Random();

    public Horse() {
        super("Horse", 75, 0, 22, 8, "assets/sprites/enemies/horse/");
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
        return "Horse — stubborn bruiser.";
    }
}
