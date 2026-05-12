package enemies;

import core.Character;
import java.util.Random;

public class Fish extends Character {
    private static final Random RNG = new Random();

    public Fish() {
        super("Fish", 55, 0, 14, 4, "assets/sprites/enemies/fish/");
    }

    @Override
    public int attack(Character target) {
        int dmg = getAttackPower() + RNG.nextInt(8);
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
        return "Fish — slippery brawler.";
    }
}
