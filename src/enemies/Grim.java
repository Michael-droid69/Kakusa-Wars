package enemies;

import core.Character;
import java.util.Random;

public class Grim extends Character {
    private static final Random RNG = new Random();

    public Grim() {
        super("Grim", 90, 0, 20, 10, "assets/sprites/enemies/grim/");
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
        return "Grim — relentless shadow brute.";
    }
}
