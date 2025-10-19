package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.CustomArena;
import fr.codinbox.footballplugin.game.CustomGameCriterias;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.ChatUtils;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateCustomFiller implements IFiller {

    private static final String EARTH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=";
    private static final String GREEN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19";

    private CustomGameCriterias criterias;

    public CreateCustomFiller(CustomGameCriterias criterias) {
        this.criterias = criterias;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        provider.setItem(2, 2, ClickableItem.on(getMapItem(), onClick -> {
            FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCustomMapMenu(player), player);
        }));

        provider.setItem(2, 3, ClickableItem.on(getSpectatorsItem(), onClick -> {
            criterias.setAllowSpectators(!criterias.isAllowSpectators());
            provider.setItem(2, 3, ClickableItem.previous(getSpectatorsItem()));
        }));

        provider.setItem(2, 4, ClickableItem.on(getPasswordItem(), onClick -> {
            switch (onClick.getClick()) {
                case LEFT:
                    player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_HURT, 1f, 1f);
                    player.sendMessage("§aPlease enter a new password:");
                    ChatUtils.doOnNextMessage(player, event -> {
                        criterias.setPassword(event.getMessage());
                        player.sendMessage("§aNew password set!");
                        FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCustomCreateMenu(player), player);
                    });
                    player.closeInventory();
                    break;

                case RIGHT:
                    criterias.setPassword("");
                    provider.setItem(2, 4, ClickableItem.previous(getPasswordItem()));
                    break;
            }
        }));

        provider.setItem(2, 8, ClickableItem.on(getCreateButtonItem(), onClick -> {
            player.getInventory().clear();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
            player.sendMessage(new LanguageManager.Phrase(LanguageKey.CUSTOM_ARENA_CREATING_MESSAGE).toString());
            CustomGameCriterias playerCriterias = FootballPlugin.INSTANCE.getArenaManager().getCriterias(player);
            CustomArena arena = new CustomArena(FootMode.CUSTOM,
                    FootballPlugin.INSTANCE.getWorldService().loadWorld(playerCriterias.getMap(), false),
                    player.getUniqueId(), playerCriterias.getPassword(), playerCriterias.isAllowSpectators());
            arena.joinPlayer(player, false);
        }));

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getMapItem() {
        FootMap map = criterias.getMap();
        int blueTeamSpawns = map.getTeamSpawns().get(FootTeamQualifier.BLUE).size();
        int redTeamSpawns = map.getTeamSpawns().get(FootTeamQualifier.RED).size();
        return new ItemBuilder(CustomHeads.fromBase64(EARTH_TEXTURE))
                .setName("§b§uMap: §r§b" + map.getName())
                .addLoreLine("§r")
                .addLoreLine("§7Number of spawns: §b" + blueTeamSpawns + "§8+§c" + redTeamSpawns)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to change map")
                .toItemStack();
    }

    private ItemStack getSpectatorsItem() {
        boolean allowSpectators = criterias.isAllowSpectators();
        String allowSpectatorsValue = allowSpectators ? "§aYes" : "§cNo";
        return new ItemBuilder((allowSpectators ? Material.GREEN_CONCRETE : Material.RED_CONCRETE), 1)
                .setName("§bAllow spectators")
                .addLoreLine("§r")
                .addLoreLine("§7State: " + allowSpectatorsValue)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to switch value")
                .toItemStack();
    }

    private ItemStack getPvpItem() {
        boolean allowPvp = criterias.isAllowPlayerDamages();
        String allowPvpValue = allowPvp ? "§aYes" : "§cNo";
        return new ItemBuilder((allowPvp ? Material.GREEN_CONCRETE : Material.RED_CONCRETE), 1)
                .setName("§bAllow PvP")
                .addLoreLine("§r")
                .addLoreLine("§7State: " + allowPvpValue)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to switch value")
                .toItemStack();
    }

    private ItemStack getPasswordItem() {
        String passwordSet = criterias.hasPassword() ? "§aYes" : "§cNo";
        String passwordValue = criterias.hasPassword() ? criterias.getPassword() : "NO PASSWORD";
        return new ItemBuilder(Material.OAK_SIGN)
                .setName("§bPassword")
                .addLoreLine("§r")
                .addLoreLine("§7Set: " + passwordSet)
                .addLoreLine("§7Value: §6" + passwordValue)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to change password")
                .addLoreLine("§eRight-click to delete password")
                .toItemStack();
    }

    private ItemStack getCreateButtonItem() {
        return new ItemBuilder(CustomHeads.fromBase64(GREEN_TEXTURE))
                .setName("§aCreate the custom game")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to create your custom game!")
                .toItemStack();
    }


}
