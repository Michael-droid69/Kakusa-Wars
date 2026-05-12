package core;

public class Item {

    // All final — items cannot be modified after creation (Encapsulation)
    private final String name;
    private final String description;
    private final String effectType;   // "heal" | "mana" | "revive" | "atk_buff" | "def_buff"
    private final int    effectValue;
    private final int    shopCost;

    public Item(String name, String description,
                String effectType, int effectValue, int shopCost) {
        this.name        = name;
        this.description = description;
        this.effectType  = effectType;
        this.effectValue = effectValue;
        this.shopCost    = shopCost;
    }

    // Applies this item's effect directly to a character
    public String applyTo(Character target) {
        return switch (effectType) {
            case "heal" -> {
                target.heal(effectValue);
                yield target.getName() + " recovered " + effectValue + " HP!";
            }
            case "mana" -> {
                target.restoreMana(effectValue);
                yield target.getName() + " restored " + effectValue + " MP!";
            }
            case "revive" -> {
                if (!target.isAlive()) {
                    target.setAlive(true);
                    target.heal(effectValue);
                    yield target.getName() + " was revived with " + effectValue + " HP!";
                }
                yield target.getName() + " is already alive!";
            }
            case "atk_buff" -> {
                target.setAttackPower(target.getAttackPower() + effectValue);
                yield target.getName() + " ATK +" + effectValue + "!";
            }
            case "def_buff" -> {
                target.setDefensePower(target.getDefensePower() + effectValue);
                yield target.getName() + " DEF +" + effectValue + "!";
            }
            default -> "Nothing happened.";
        };
    }

    // Getters only — no setters (fields are final)
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getEffectType()  { return effectType; }
    public int    getEffectValue() { return effectValue; }
    public int    getShopCost()    { return shopCost; }

    @Override
    public String toString() {
        return name + " — " + description + "  [" + shopCost + "g]";
    }
}