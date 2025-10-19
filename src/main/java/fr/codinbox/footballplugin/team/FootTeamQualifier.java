package fr.codinbox.footballplugin.team;

import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.inventory.ItemStack;

public enum FootTeamQualifier {

    BLUE("§b", "BLUE_TEAM_NAME", new ItemBuilder(Material.BLUE_CONCRETE, 1).toItemStack(), DyeColor.BLUE, Color.BLUE, BarColor.BLUE),
    RED("§c", "RED_TEAM_NAME", new ItemBuilder(Material.RED_CONCRETE, 1).toItemStack(), DyeColor.RED, Color.RED, BarColor.RED);

    private final String colorCode;
    private String languageName;
    private ItemStack item;
    private DyeColor dyeColor;
    private Color color;
    private BarColor barColor;

    FootTeamQualifier(String colorCode, String languageName, ItemStack item, DyeColor dyeColor, Color color, BarColor barColor) {
        this.colorCode = colorCode;
        this.languageName = languageName;
        this.item = item;
        this.dyeColor = dyeColor;
        this.color = color;
        this.barColor = barColor;
    }

    public static void initLanguage(LanguageManager languageManager) {
        for(FootTeamQualifier qualifier : values())
            qualifier.languageName = languageManager.getPhrase(LanguageKey.toLanguageKey(qualifier.languageName));
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public FootTeamQualifier getOtherTeam() {
        switch (this) {
            case BLUE:
                return RED;
            case RED:
                return BLUE;
        }
        return null;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public Color getColor() {
        return color;
    }

    public BarColor getBarColor() {
        return barColor;
    }

}
