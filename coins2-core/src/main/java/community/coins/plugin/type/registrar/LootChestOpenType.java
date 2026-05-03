package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class LootChestOpenType extends EventType {
    public LootChestOpenType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasLocationWorld();
        super(coins, service, "loot_chest_open", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#loot_chest_open

    @EventHandler
    void onLootGenerateEvent(LootGenerateEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // todo maybe add filter for block/entity type: minecart_chest, barrel, etc.
        var filter = createForm()
            .withInitiatorEntity(player)
            .withLocationWorld(player.getWorld())
            .build();

        filterEvent(filter).thenConsume(coins -> coins.forEach(coin -> event.getLoot().add(coin)));
    }
}
