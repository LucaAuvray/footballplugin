package fr.codinbox.footballplugin.inventory;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.team.FootTeamQualifier;

public class SpawnEditorMenu {

    private FootballPlugin plugin;
    private MinecraftWorld map;
    private FootMap footMap;
    private FootTeamQualifier footTeamQualifier;

    public SpawnEditorMenu(FootballPlugin plugin, MinecraftWorld map, FootTeamQualifier footTeamQualifier) {
        this.plugin = plugin;
        this.map = map;
        this.footMap = map.getParentMap();
        this.footTeamQualifier = footTeamQualifier;
    }

    /*@Override
    public Inventory createInventory(Player player) {
        return Bukkit.createInventory(null, 9 * 3, new LanguageManager.Phrase(plugin.getLanguageManager(), LanguageKey.GUI_LOCATION_EDITOR_TITLE)
            .replaceVar("name", this.map.getParentMap().getName())
            .replaceVar("team", this.footTeamQualifier.getLanguageName())
            .toString()
        );
    }

    @Override
    public Inventory addItems(Inventory inventory, Player player) {
        inventory.clear();

        ArrayList<SerializablePosition> spawnLocations = this.footMap.getTeamSpawns().get(this.footTeamQualifier);
        for (int i = 0; i < spawnLocations.size(); i++) {
            SerializablePosition location = spawnLocations.get(i);
            inventory.setItem(i, getSpawnItem(i + 1, location.toLocation(this.map.getWorld())));
        }
        inventory.setItem(spawnLocations.size(), getCreateSpawnItem());

        return inventory;
    }

    @Override
    public void openInventory(Player player) {
        Inventory inventory = addItems(createInventory(player), player);
        player.openInventory(inventory);
        FootballPlugin.INSTANCE.getOpenedInventories().put(player, this);
    }

    @Override
    public void onClick(Inventory inventory, ItemStack item, int slot, ClickType clickType, Player player) {
        switch (item.getType()) {
            case WOOD_BUTTON:
                player.closeInventory();

                Location playerLocation = player.getLocation();
                SerializablePosition serializablePosition = new SerializablePosition(playerLocation);

                this.footMap.addTeamSpawn(this.footTeamQualifier, serializablePosition);

                player.sendMessage(new LanguageManager.Phrase(plugin.getLanguageManager(), LanguageKey.SPAWNPOINT_SET)
                        .replaceVar("current", String.valueOf(this.footMap.getTeamSpawns().get(this.footTeamQualifier).size()))
                        .replaceVar("max", String.valueOf(this.footMap.getMode().getNumberOfSpawns() > 0 ? this.footMap.getMode().getNumberOfSpawns() : "∞"))
                        .toString());
                break;

            case BEACON:
                SerializablePosition spawnLocation = this.footMap.getTeamSpawn(this.footTeamQualifier, slot);

                switch (clickType) {
                    case LEFT:
                        Location sLocation = spawnLocation.toLocation(map.getWorld());
                        player.teleport(sLocation);
                        player.closeInventory();
                        break;

                    case RIGHT:
                        this.footMap.deleteTeamSpawn(footTeamQualifier, spawnLocation);
                        break;
                }

                break;
        }

        addItems(inventory, player);
    }

    private ItemStack getSpawnItem(int number, Location location) {
        return new ItemBuilder(Material.BEACON, number)
                .setName("§bSpawn #" + number)
                .addLoreLine("§r")
                .addLoreLine("§7Coordinates:")
                .addLoreLine("§7X: " + location.getX())
                .addLoreLine("§7Y: " + location.getY())
                .addLoreLine("§7Z: " + location.getZ())
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to teleport")
                .addLoreLine("§eRight-click to delete")
                .toItemStack();
    }

    private ItemStack getCreateSpawnItem() {
        int numberOfSpawns = this.footMap.getTeamSpawns().get(this.footTeamQualifier).size();
        int maxSpawns = this.footMap.getMode().getNumberOfSpawns();
        int numberOfSpawnsLeft = maxSpawns - numberOfSpawns;

        if(numberOfSpawnsLeft > 0 || maxSpawns == -1)
            return new ItemBuilder(Material.WOOD_BUTTON)
                    .setName("§aCreate a new spawn")
                    .addLoreLine("§e" + (maxSpawns > -1 ? numberOfSpawnsLeft : "∞") + " remaining")
                    .toItemStack();
        else
            return new ItemStack(Material.AIR);
    }*/


}
