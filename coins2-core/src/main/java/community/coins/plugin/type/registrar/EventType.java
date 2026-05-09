package community.coins.plugin.type.registrar;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.drops.CoinDropAction;
import community.coins.plugin.drops.DefinedDrop;
import community.coins.plugin.economy.DepositType;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.filter.EventFilterForm;
import community.coins.plugin.type.filter.FilterContract;
import community.coins.plugin.type.filter.FilterContractBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Eli
 * @since May 01, 2026
 */
public abstract class EventType implements Listener {
    private final CoinsCore coins;
    private final String identifier;
    private final FilterContract filterContract; // the filter that is allowed for this event type

    // todo could also be expanded by other plugins, so a registrar of some sorts

    /**
     * @param identifier the identifier of the event type (i.e. "advancement_done")
     * @param contract this filter is like a contract of what's allowed.
     *                 it checks this filter contract when parsing a defined drop
     */
    @NullMarked
    public EventType(CoinsCore coins, EventTypeService service, String identifier, Function<FilterContractBuilder, FilterContractBuilder> contract) {
        this.coins = coins;
        this.identifier = identifier.toLowerCase();
        this.filterContract = contract.apply(new FilterContractBuilder(this)).build();
        service.registerEventType(this);
    }

    private final Map<String, DefinedDrop> registeredDrops = new ConcurrentHashMap<>();

    public void registerDrop(DefinedDrop drop) {
        registeredDrops.put(drop.getIdentifier(), drop);
    }

    public Collection<DefinedDrop> getRegisteredDrops() {
        return registeredDrops.values();
    }

    public void clearRegisteredDrops() {
        registeredDrops.clear();
    }

    /// this is handled once per event type, and fans out into different CoinDropActions for every DefinedDrop
    private void handleEvent(EventFilterForm filter, Consumer<List<CoinDropAction>> actions) {
        // filter it per different registered DefinedDrops on this EventType
        List<CoinDropAction> coinActions = new LinkedList<>();
        for (DefinedDrop drop : getRegisteredDrops()) {
            if (!filter.isAllowed(drop)) {
                continue; // remove those that do not meet the filter
            }

            // generate a coin action, but this can be empty if i.e. chance didn't allow it
            Optional<CoinDropAction> coinAction = drop.generateCoinAction(coins, filter);
            if (coinAction.isEmpty()) {
                coins.debug("Chance canceled coins to consume for '%s'".formatted(identifier));
                continue;
            }

            coins.debug("Dropping or consuming coin(s) for '%s'".formatted(identifier));
            // filtered and allowed event; add to list
            coinActions.add(coinAction.get());
        }

        actions.accept(coinActions);
    }

    public void callEvent(EventFilterForm filter, @NotNull Block dropLocation) {
        callEvent(filter, dropLocation.getLocation().add(.5, .5, .5));
    }

    public void callEvent(EventFilterForm filter, @NotNull Location dropLocation) {
        handleEvent(filter, actions -> {
            for (CoinDropAction action : actions) {
                DepositType depositType = action.getDefinedDrop().getDepositType();
                Entity initiator = action.getFilter().getInitiatorEntity(); // can be null if 'any'

                for (ItemStack coin : action.getCoinItems()) {
                    if (depositType == DepositType.BALANCE && initiator instanceof Player player) {
                        coins.getEconomyService().depositCoin(player, coin);
                    }
                    else if (depositType == DepositType.INVENTORY && initiator instanceof Player player) {
                        player.getInventory().addItem(coin);
                    }
                    else if (dropLocation.getWorld() != null) {
                        dropLocation.getWorld().dropItemNaturally(dropLocation, coin);
                    }

                    coins.getMetrics().registerCoinCreate(coin.getAmount());
                }
            }
        });
    }

    public void callEvent(EventFilterForm filter, Consumer<List<ItemStack>> coinItems) {
        handleEvent(filter, actions -> actions.forEach(action -> coinItems.accept(action.getCoinItems())));
    }

    @CheckReturnValue
    protected EventFilterForm createFilter() {
        return new EventFilterForm(coins, identifier);
    }

    public FilterContract getFilterContract() {
        return filterContract;
    }

    /// @return already lowercase identifier
    public String getIdentifier() {
        return identifier;
    }
}
