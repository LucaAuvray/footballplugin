package fr.codinbox.footballplugin.inventory.fillers;

import com.google.common.collect.Lists;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.game.CustomArena;
import fr.codinbox.footballplugin.game.GameState;
import fr.codinbox.footballplugin.inventory.*;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.utils.ChatUtils;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CustomListFiller implements IFiller {

    public static final String BLACK_FORWARD_ARROW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=";
    public static final String BLACK_BACKWARD_ARROW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=";
    private static final String GREEN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJkMTQ1YzkzZTVlYWM0OGE2NjFjNmYyN2ZkYWZmNTkyMmNmNDMzZGQ2MjdiZjIzZWVjMzc4Yjk5NTYxOTcifX19";

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        ArrayList<Arena> arenas = Lists.newArrayList(FootballPlugin.INSTANCE.getArenaManager().getArenasByMode(FootMode.CUSTOM));

        ClickableItem[] items = new ClickableItem[arenas.size()];
        for(int i = 0; i < items.length; i++) {
            final CustomArena arena = (CustomArena) arenas.get(i);
            items[i] = ClickableItem.on(getCustomItem(arena), onClick -> {
                if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                    player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                    return;
                }

                if(arena.isPlaying()) {
                    if(!arena.isAllowingSpectators()) {
                        player.sendMessage(new LanguageManager.Phrase(LanguageKey.CUSTOM_NO_SPECTATORS_MESSAGE).toString());
                        return;
                    }
                }

                if(arena.getPassword() == null || arena.getPassword().isEmpty()) {
                    if(!arena.isFull())
                        arena.joinPlayer(player, false);
                    else {
                        player.sendMessage(new LanguageManager.Phrase(LanguageKey.ARENA_FULL).toString());
                    }
                }
                else {
                    player.closeInventory();
                    player.sendMessage(new LanguageManager.Phrase(LanguageKey.PROMPT_PASSWORD_MESSAGE).toString());
                    ChatUtils.doOnNextMessage(player, event -> {
                        if(arena.getPassword().equals(event.getMessage())) {
                            if(!arena.isFull())
                                arena.joinPlayer(player, false);
                        }
                        else
                            player.sendMessage(new LanguageManager.Phrase(LanguageKey.WRONG_PASSWORD_MESSAGE).toString());
                    });
                }
            });
        }

        SimplePagination pagination = provider.newPagination(items, 21,
                new InventorySlotGroup(new InventorySlot(2, 2), new InventorySlot(4, 8)),
                new CubicIterator());

        if(pagination.getPageNumber() > 0)
            provider.setItem(5, 6, ClickableItem.on(createNextArrowItem(), onClick -> {
                pagination.nextPage();
                pagination.render();
            }));

        if(player.hasPermission("football.custom") || player.hasPermission("football.admin")) {
            provider.setItem(5, 5, ClickableItem.on(createCreateButtonItem(), onClick -> {
                if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                    player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                    return;
                }

                if(FootballPlugin.INSTANCE.getArenaManager().getCriterias(player) == null)
                    FootballPlugin.INSTANCE.getArenaManager().registerNewCriterias(player);
                FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCustomCreateMenu(player), player);
            }));
        }

        pagination.onUpdate(simplePagination -> {

            if(simplePagination.getPageNumber() <= 0) {
                provider.setItem(5, 4, ClickableItem.empty(new ItemStack(Material.AIR)));
            }
            else {
                provider.setItem(5, 4, ClickableItem.on(createPreviousArrowItem(), onClick -> {
                    pagination.previousPage();
                    pagination.render();
                }));
            }

            if(simplePagination.getPageNumber() >= simplePagination.getNumberOfPages()) {
                provider.setItem(5, 6, ClickableItem.empty(new ItemStack(Material.AIR)));
            }
            else {
                provider.setItem(5, 6, ClickableItem.on(createNextArrowItem(), onClick -> {
                    pagination.nextPage();
                    pagination.render();
                }));
            }

        });

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack createPreviousArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(BLACK_BACKWARD_ARROW_TEXTURE))
                .setName("§bPrevious page")
                .toItemStack();
    }

    private ItemStack createNextArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(BLACK_FORWARD_ARROW_TEXTURE))
                .setName("§bNext page")
                .toItemStack();
    }

    private ItemStack createCreateButtonItem() {
        return new ItemBuilder(CustomHeads.fromBase64(GREEN_TEXTURE))
                .setName("§aCreate a custom game")
                .toItemStack();
    }

    private ItemStack getCustomItem(Arena arena) {
        ItemBuilder item = new ItemBuilder(Material.PLAYER_HEAD, 1)
                .setSkullOwner(Bukkit.getPlayer(arena.getOwner()).getName())
                .setName("§a" + Bukkit.getPlayer(arena.getOwner()).getName() + "'s game")
                .addLoreLine("§8" + arena.getAllPlayers().size() + " connected players")
                .addLoreLine("§r");

        if(arena.getPassword() != null && !arena.getPassword().isEmpty())
            item.addLoreLine("§cThis game is password-protected");

        item.addLoreLine("§eLeft-click to join the game!");

        return item.toItemStack();
    }

}
