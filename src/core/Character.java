package core;

import java.util.ArrayList;
import java.util.List;

public abstract class Character {

    // ─────────────────────────────────────────
    // ALL PRIVATE FIELDS  (Encapsulation)
    // ─────────────────────────────────────────
    private String  name;
    private int     hp;
    private int     maxHp;
    private int     mana;
    private int     maxMana;
    private int     attackPower;
    private int     defensePower;
    private int     speed;          // used by Rogue/Archer passives
    private int     gold;
    private boolean isAlive;
    private boolean isTaunted;      // set by enemy taunt, cleared each turn
    private List<Skill> skills;
    private String  spriteFolder;   // e.g. "assets/sprites/arthur/"

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────
    // Full constructor (speed provided)
    public Character(String name,
                     int maxHp, int maxMana,
                     int attackPower, int defensePower,
                     int speed, String spriteFolder) {
        this.name         = name;
        this.maxHp        = maxHp;
        this.hp           = maxHp;           // start at full HP
        this.maxMana      = maxMana;
        this.mana         = maxMana;         // start at full mana
        this.attackPower  = attackPower;
        this.defensePower = defensePower;
        this.speed        = speed;
        this.gold         = 0;
        this.isAlive      = true;
        this.isTaunted    = false;
        this.spriteFolder = spriteFolder;
        this.skills       = new ArrayList<>();
    }

    // Convenience constructor (legacy subclasses currently pass 6 args, missing speed)
    public Character(String name,
                     int maxHp, int maxMana,
                     int attackPower, int defensePower,
                     String spriteFolder) {
        this(name, maxHp, maxMana, attackPower, defensePower, 10, spriteFolder);
    }

    // ─────────────────────────────────────────
    // MANA INIT HELPER
    // Characters start with low mana (25% of max) so only the
    // cheapest skill is available at turn 1. Mana grows each turn.
    // ─────────────────────────────────────────
    protected void initStartingMana() {
        // Start at 25% of max mana (minimum 15 so basic skills are reachable quickly)
        this.mana = Math.max(15, this.maxMana / 4);
    }

    // ─────────────────────────────────────────
    // ABSTRACT METHODS  (Abstraction + Polymorphism)
    // Every subclass MUST override these three
    // ─────────────────────────────────────────
    public abstract int    attack(Character target);
    public abstract int    useSkill(int skillIndex, Character target);
    public abstract void   applyPassive();
    public abstract String getClassDescription();

    // ─────────────────────────────────────────
    // SHARED COMBAT LOGIC
    // ─────────────────────────────────────────
    public void takeDamage(int rawDamage) {
        int effective = Math.max(1, rawDamage - this.defensePower);
        this.hp = Math.max(0, this.hp - effective);
        if (this.hp == 0) this.isAlive = false;
    }

    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
        if (amount > 0) this.isAlive = true;   // revive counts as heal too
    }

    public void restoreMana(int amount) {
        this.mana = Math.min(this.maxMana, this.mana + amount);
    }

    // Spends mana — throws custom exception if not enough
    public boolean spendMana(int cost)
            throws exceptions.NotEnoughManaException {
        if (this.mana < cost)
            throw new exceptions.NotEnoughManaException(
                name + " needs " + cost + " MP but only has " + mana + " MP!"
            );
        this.mana -= cost;
        return true;
    }

    // Convenience used by SaveManager when rebuilding state from save files.
    // Never throws; clamps at 0.
    public void spendManaUnsafe(int amount) {
        this.mana = Math.max(0, this.mana - amount);
    }

    // Called at the end of every turn for the whole party
    // Regen is 15 MP per turn so skills unlock progressively during battle
    public void regenManaPerTurn() {
        restoreMana(15);
    }

    // ─────────────────────────────────────────
    // SKILL LIST HELPERS
    // ─────────────────────────────────────────
    public void  addSkill(Skill skill)  { skills.add(skill); }
    public Skill getSkill(int index)    { return skills.get(index); }
    public int   getSkillCount()        { return skills.size(); }
    public List<Skill> getAllSkills()   { return skills; }

    // ─────────────────────────────────────────
    // GETTERS  (every field has one)
    // ─────────────────────────────────────────
    public String  getName()          { return name; }
    public int     getHp()            { return hp; }
    public int     getMaxHp()         { return maxHp; }
    public int     getMana()          { return mana; }
    public int     getMaxMana()       { return maxMana; }
    public int     getAttackPower()   { return attackPower; }
    public int     getDefensePower()  { return defensePower; }
    public int     getSpeed()         { return speed; }
    public int     getGold()          { return gold; }
    public boolean isAlive()          { return isAlive; }
    public boolean isTaunted()        { return isTaunted; }
    public String  getSpriteFolder()  { return spriteFolder; }

    // ─────────────────────────────────────────
    // SETTERS  (only fields that need external changes)
    // ─────────────────────────────────────────
    public void setName(String name)          { this.name = name; }
    public void setMaxHp(int maxHp)           { this.maxHp = Math.max(1, maxHp); }
    public void setMaxMana(int maxMana)       { this.maxMana = Math.max(0, maxMana); }
    public void setAttackPower(int val)       { this.attackPower = Math.max(0, val); }
    public void setDefensePower(int val)      { this.defensePower = Math.max(0, val); }
    public void setSpeed(int val)             { this.speed = Math.max(0, val); }
    public void setTaunted(boolean taunted)   { this.isTaunted = taunted; }
    public void addGold(int amount)           { this.gold += amount; }
    public void spendGold(int amount)         { this.gold = Math.max(0, this.gold - amount); }
    public void setAlive(boolean alive)       { this.isAlive = alive; }

    // ─────────────────────────────────────────
    // DEBUG / DISPLAY
    // ─────────────────────────────────────────
    @Override
    public String toString() {
        return String.format("[%s | HP:%d/%d | MP:%d/%d | ATK:%d | DEF:%d | SPD:%d]",
            name, hp, maxHp, mana, maxMana, attackPower, defensePower, speed);
    }
}
