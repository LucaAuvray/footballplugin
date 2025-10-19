package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.game.CustomArena;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomTeamFiller implements IFiller {

    private final String BLUE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJjZDI3MmVlYjM4YmY3ODNhOThhNDZmYTFlMmU4ZDQ2MmQ4NTJmYmFhZWRlZjBkY2UyYzFmNzE3YTJhIn19fQ==";
    private final String RED_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZkZTNiZmNlMmQ4Y2I3MjRkZTg1NTZlNWVjMjFiN2YxNWY1ODQ2ODRhYjc4NTIxNGFkZDE2NGJlNzYyNGIifX19";

    private CustomArena arena;

    public CustomTeamFiller(CustomArena arena) {
        this.arena = arena;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        provider.setItem(2, 2, ClickableItem.on(getAutoItem(), onClick -> {
            FootTeamQualifier smallestTeam = arena.getTeamByQualifier(FootTeamQualifier.BLUE).getPlayersUuid().size() <= arena.getTeamByQualifier(FootTeamQualifier.RED).getPlayersUuid().size() ? FootTeamQualifier.BLUE : FootTeamQualifier.RED;
            arena.joinTeam(player, smallestTeam);
        }));

        provider.setItem(2, 3, ClickableItem.on(getBlueTeamItem(), onClick -> {
            arena.joinTeam(player, FootTeamQualifier.BLUE);
        }));

        provider.setItem(2, 4, ClickableItem.on(getRedTeamItem(), onClick -> {
            arena.joinTeam(player, FootTeamQualifier.RED);
        }));

        provider.setItem(2, 8, ClickableItem.on(getSpectatorItem(), onClick -> {
            arena.joinSpectator(player);
        }));

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getAutoItem() {
        return new ItemBuilder(Material.OAK_BUTTON)
                .setName("§dAuto")
                .toItemStack();
    }

    private ItemStack getBlueTeamItem() {
        return new ItemBuilder(CustomHeads.fromBase64(BLUE_TEXTURE))
                .setName("§bJoin Blue team")
                .addLoreLine("§8" + arena.getTeamByQualifier(FootTeamQualifier.BLUE).getPlayersUuid().size() + " players")
                .toItemStack();
    }

    private ItemStack getRedTeamItem() {
        return new ItemBuilder(CustomHeads.fromBase64(RED_TEXTURE))
                .setName("§cJoin Red team")
                .addLoreLine("§8" + arena.getTeamByQualifier(FootTeamQualifier.RED).getPlayersUuid().size() + " players")
                .toItemStack();
    }

    private ItemStack getSpectatorItem() {
        return new ItemBuilder(Material.FEATHER)
                .setName("§9Spectate the match")
                .toItemStack();
    }

}
