package enemies;

import core.Character;
import java.util.Random;

public class Witch extends Character {
    private static final Random RNG = new Random();

    public Witch() {
        super("Witch", 70, 0, 20, 8, "assets/sprites/enemies/witch/");
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
        return "Witch — eerie hexer.";
    }
}
