package community.coins.plugin.misc;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.coin.DefinedCoin;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.SplittableRandom;

/**
 * @author Eli
 * @since June 16, 2026
 */
public final class CoinsPublicApi {
    private final CoinsCore coins;
    public CoinsPublicApi(CoinsCore coins) {
        this.coins = coins;
    }

    private static final SplittableRandom RANDOM = new SplittableRandom();

    public void dropCoins(@NotNull DefinedCoin coin, @NotNull Location location, double radius, int amount, double value) {
        var world = location.getWorld();
        if (world == null) {
            return;
        }

        coins.getScheduler().runLocationTaskRepeated(location, amount, 1, () -> {
            ItemStack itemStack = coin.getItemStackClone();
            ItemMeta meta = itemStack.getItemMeta();
            coins.getCoinMeta().setCoinValue(meta, value);
            itemStack.setItemMeta(meta);

            Item item = world.dropItem(location, itemStack);

            item.setPickupDelay(30);
            item.setVelocity(new Vector(
                (RANDOM.nextDouble() - 0.5) * radius / 10,
                RANDOM.nextDouble() * radius / 5,
                (RANDOM.nextDouble() - 0.5) * radius / 10
            ));
        });
    }
}
