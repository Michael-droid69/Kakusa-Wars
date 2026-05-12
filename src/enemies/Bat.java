package enemies;

import core.Character;
import java.util.Random;

public class Bat extends Character {

    public Bat() {
        super("Bat", 40, 0, 12, 3, "assets/sprites/enemies/bat/");
    }

    @Override public int     attack(Character target) {
        int dmg = getAttackPower() + new Random().nextInt(8);
        target.takeDamage(dmg); return dmg;
    }
    @Override public int     useSkill(int i, Character target) { return attack(target); }
    @Override public void    applyPassive()        {} // no passive
    @Override public String  getClassDescription() { return "Bat — Sexy and Pretty."; }
}