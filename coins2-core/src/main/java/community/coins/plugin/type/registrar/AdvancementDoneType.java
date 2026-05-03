package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.api.EventType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

/**
 * @author Eli
 * @since April 29, 2026
 */
public final class AdvancementDoneType extends EventType {
    public AdvancementDoneType(CoinsCore coins, EventTypeService service) {
        var filter = service.filterBuilder()
            .hasInitiatorPlayer()
            .hasTargetType()
            .hasLocationWorld();
        super(coins, service, "advancement_done", filter);
    }

    // event: 'advancement_done'
    // filters:
    //   initiator:
    //     enabled: Boolean
    //     permission: String
    //   target:
    //     type: List<String>   (advancement types)
    //   location:
    //     disabled-worlds: List<String>
    // coins: ...

    @EventHandler(ignoreCancelled = true)
    void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        // todo NoSuchMethodError
//        Advancement advancement = event.getAdvancement();
//        if (advancement.getDisplay() == null) {
//            return;
//        }
//
//        var filter = createForm()
//            .withInitiatorEntity(player)
//            .withTargetType(advancement)
//            .withLocationWorld(player.getLocation().getWorld())
//            .build();
//
//        filterEvent(filter).thenDrop(player.getLocation());
    }
}
