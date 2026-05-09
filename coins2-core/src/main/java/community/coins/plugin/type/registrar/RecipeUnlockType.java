package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
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
        super(coins, service, "recipe_unlock", filter -> filter
            .hasInitiatorPlayer()
            .hasLocationWorld()
        );
    }

    // https://github.com/justEli/Coins2/wiki/Defining-drop-filters#recipe_unlock

    @EventHandler(ignoreCancelled = true)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();
        if (!advancement.getCriteria().contains("has_the_recipe")) {
            return;
        }

        // todo #setTargetType for the item type(s) of the recipe
        var filter = createFilter()
            .withInitiatorEntity(player)
            .withLocationWorld(player.getWorld());

        callEvent(filter, player.getLocation());
    }
}
