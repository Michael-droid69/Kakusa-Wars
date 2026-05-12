package core;

public class Skill {

    private final String name;
    private final String description;
    private final int    manaCost;
    private final double damageMultiplier;
    private final String type;
    // type options: "damage" | "damage_all" | "heal" | "buff_atk" | "buff_def" | "debuff"

    public Skill(String name, String description,
                 int manaCost, double damageMultiplier, String type) {
        this.name             = name;
        this.description      = description;
        this.manaCost         = manaCost;
        this.damageMultiplier = damageMultiplier;
        this.type             = type;
    }

    // Getters only — skills are immutable once created
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public int    getManaCost()         { return manaCost; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public String getType()             { return type; }

    @Override
    public String toString() {
        return String.format("%s  [%d MP] — %s", name, manaCost, description);
    }
}