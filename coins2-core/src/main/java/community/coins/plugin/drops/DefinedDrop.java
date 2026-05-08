package community.coins.plugin.drops;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.filter.EventFilterConfig;
import community.coins.plugin.type.filter.EventFilterForm;
import community.coins.plugin.util.BlockCache;
import community.coins.plugin.util.BlockPosition;
import community.coins.plugin.util.Util;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eli
 * @since May 01, 2026
 */
@NullMarked
public final class DefinedDrop {
    private final String identifier;
    private final EventFilterConfig eventFilterConfig;
    private final DefinedCoinDrop definedCoinDrop;

    public DefinedDrop(String identifier, EventFilterConfig filterConfig, DefinedCoinDrop coinDrop) {
        this.identifier = identifier.toLowerCase();
        this.eventFilterConfig = filterConfig;
        this.definedCoinDrop = coinDrop;
    }

    private static final SplittableRandom RANDOM = new SplittableRandom();

    public String getIdentifier() {
        return identifier;
    }

    public EventFilterConfig getEventFilterConfig() {
        return eventFilterConfig;
    }

    // todo implement
    public DepositType getDepositType() {
        return DepositType.DROP;
    }

    public Optional<CoinDropAction> generateCoinAction(CoinsCore coins, EventFilterForm form) {
        Optional<AmountedCoin> amountedCoin = definedCoinDrop.getRandomPick();
        if (amountedCoin.isEmpty()) {
            return Optional.empty(); // chance didn't allow it
        }

        double min = amountedCoin.get().minValue();
        double max = amountedCoin.get().maxValue();

        // todo drop-each-coin can be programmed here (currently always only 1 in list)

        List<ItemStack> items = new ArrayList<>();

        // create coin
        ItemStack coin = amountedCoin.get().coin().getItemStackClone();
        ItemMeta meta = coin.getItemMeta();

        double value = Util.toRoundedMoneyDecimals(
            min == max? min : RANDOM.nextDouble(min, max),
            amountedCoin.get().decimals()
        );

        coins.getCoinMeta().setCoinValue(meta, value);
        coin.setItemMeta(meta);

        // add coin to drop action
        items.add(coin);
        return Optional.of(new CoinDropAction(this, form, items));
    }

    // location cooldown caching is done per different drop

    public void cleanUpLocationCache() {
        locationLimitCache.entrySet().removeIf(entry -> !entry.getValue().isWithinConfiguredTime());
    }

    private final Map<BlockPosition, BlockCache> locationLimitCache = new ConcurrentHashMap<>();

    public boolean isLocationAvailableAndSet(Location location, int capAmount, int durationMillis) {
        if (capAmount < 1) {
            return true;
        }

        BlockPosition position = new BlockPosition(location);
        BlockCache cache = locationLimitCache.computeIfAbsent(position, _ -> new BlockCache(durationMillis));

        if (cache.isWithinConfiguredTime()) {
            return cache.getAndIncrement() < capAmount;
        }

        cache.getAndIncrement();
        return true;
    }
}
