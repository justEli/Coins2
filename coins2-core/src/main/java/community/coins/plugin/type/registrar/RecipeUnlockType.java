package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.GameMode;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class RecipeUnlockType extends EventType {
    public RecipeUnlockType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasLocationWorld();
        super(coins, service, "recipe_unlock", filter);
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#recipe_unlock

    @EventHandler(ignoreCancelled = true)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Advancement advancement = event.getAdvancement();
        if (!advancement.getCriteria().contains("has_the_recipe")) {
            return;
        }

        // todo #setTarget for Material
        var filter = createForm()
            .withInitiatorEntity(player)
            .withLocationWorld(player.getWorld())
            .build();

        filterEvent(filter).thenDrop(player.getLocation());
    }
}
