package community.coins.plugin.item;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.SplittableRandom;
import java.util.UUID;

/**
 * @author Eli
 * @since April 30, 2026
 */
public final class CoinMeta {
    private final CoinsCore coins;

    private final NamespacedKey valueKey; // stores a coin's value
    private final NamespacedKey currencyKey; // currency of the coin
    private final NamespacedKey withdrawnKey; // stores the withdrawer's uuid of the coin
    private final NamespacedKey uniqueKey; // to add a random value so it doesn't stack
    private final NamespacedKey glowKey; // makes the coin glow
    private final NamespacedKey hologramKey; // set the display name visible
    private final NamespacedKey noHopperKey; // when coin shouldn't be picked up by hoppers
    private final NamespacedKey immutableKey; // cancel name changes of coin
    private final NamespacedKey soundKey;
    private final NamespacedKey volumeKey;
    private final NamespacedKey pitchKey;

    private Scoreboard scoreboard;
    private static final String TEAM_PREFIX = "coins_glow_";

    private static final SplittableRandom RANDOM = new SplittableRandom();

    public CoinMeta(CoinsCore coins) {
        this.coins = coins;
        var manager = coins.getServer().getScoreboardManager();
        if (manager != null) {
            this.scoreboard = manager.getMainScoreboard();
        }

        this.valueKey = new NamespacedKey(coins, "value");
        this.currencyKey = new NamespacedKey(coins, "currency");
        this.withdrawnKey = new NamespacedKey(coins, "withdrawn");
        this.uniqueKey = new NamespacedKey(coins, "unique");
        this.glowKey = new NamespacedKey(coins, "glow");
        this.hologramKey = new NamespacedKey(coins, "hologram");
        this.noHopperKey = new NamespacedKey(coins, "no_hopper");
        this.immutableKey = new NamespacedKey(coins, "immutable");
        this.soundKey = new NamespacedKey(coins, "sound");
        this.volumeKey = new NamespacedKey(coins, "volume");
        this.pitchKey = new NamespacedKey(coins, "pitch");

        coins.addShutdownTask(() -> scoreboard.getTeams().forEach(team -> {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.unregister();
            }
        }));
    }

    // coin values

    public boolean isCoin(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        // a coin always has a value
        return item.getItemMeta().getPersistentDataContainer().has(valueKey, PersistentDataType.DOUBLE);
    }

    /// make sure the coin has a currency
    public void setCoinValue(ItemMeta meta, double amount) {
        if (meta == null ||  amount <= 0) {
            return;
        }

        meta.getPersistentDataContainer().set(valueKey, PersistentDataType.DOUBLE, amount);
    }

    public void setCoinCurrency(ItemMeta meta, DefinedCurrency currency) {
        if (meta == null || currency == null) {
            return;
        }

        meta.getPersistentDataContainer().set(currencyKey, PersistentDataType.STRING, currency.getIdentifier());
    }

    public OptionalDouble getCoinValue(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return OptionalDouble.empty();
        }

        Double value = item.getItemMeta().getPersistentDataContainer().get(valueKey, PersistentDataType.DOUBLE);
        return value == null? OptionalDouble.empty() : OptionalDouble.of(value);
    }

    public Optional<String> getCoinCurrency(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(item.getItemMeta().getPersistentDataContainer().get(currencyKey, PersistentDataType.STRING));
    }

    public Optional<DefinedCurrency> getCoinDefinedCurrency(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return Optional.empty();
        }

        return getCoinCurrency(item).flatMap(name -> coins.getEconomyService().getCurrency(name));
    }

    // coin withdrawal

    public Optional<UUID> getWithdrawOwner(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return Optional.empty();
        }

        String rawUuid = item.getItemMeta().getPersistentDataContainer().get(withdrawnKey, PersistentDataType.STRING);
        if (rawUuid == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(rawUuid));
        }
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public void setWithdrawOwner(ItemMeta meta, UUID uuid) {
        if (meta == null) {
            return;
        }

        meta.getPersistentDataContainer().set(withdrawnKey, PersistentDataType.STRING, uuid.toString());
    }

    // set meta of a coin

    private <P, C> void applyMeta(ItemMeta meta, NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
        if (meta != null) {
            meta.getPersistentDataContainer().set(key, type, value);
        }
    }

    private <P, C> void applyMeta(ItemStack item, NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
        if (item == null || item.getItemMeta() == null) {
            return;
        }

        var meta = item.getItemMeta();
        applyMeta(meta, key, type, value);
        item.setItemMeta(meta);
    }

    private <P, C> Optional<C> getMeta(@NotNull ItemStack item, NamespacedKey key, @NotNull PersistentDataType<P, C> type) {
        var meta = item.getItemMeta();
        if (meta == null) {
            return Optional.empty();
        }

        var container = meta.getPersistentDataContainer();
        if (!container.has(key, type)) {
            return Optional.empty();
        }

        return Optional.ofNullable(container.get(key, type));
    }

    private <P, C> Optional<C> getMeta(Item item, NamespacedKey key, @NotNull PersistentDataType<P, C> type) {
        if (item == null) {
            return Optional.empty();
        }

        return getMeta(item.getItemStack(), key, type);
    }

    // coin uniqueness (no item merge)
    // never unique in the inventory, setting it to 0 to add the characteristic when it is dropped

    /// set the item to be unique when on the ground
    public void setUniqueProperty(ItemMeta meta, boolean value) {
        if (value) {
            applyMeta(meta, uniqueKey, PersistentDataType.DOUBLE, 0D);
        }
        else if (meta != null) {
            meta.getPersistentDataContainer().remove(uniqueKey);
        }
    }

    /// set the item to be fully unique if current value is 0, only really used when ItemStack -> Item
    public void applyUniqueIfPresent(Item item) {
        Optional<Double> unique = getMeta(item, uniqueKey, PersistentDataType.DOUBLE);
        if (unique.isEmpty()) {
            return;
        }

        applyMeta(item.getItemStack(), uniqueKey, PersistentDataType.DOUBLE, RANDOM.nextDouble());
    }

    /// remove uniqueness again (set to 0 to allow in the future), i.e. when coin is picked up into inventory
    public void removeUniqueIfPresent(Item item) {
        Optional<Double> unique = getMeta(item, uniqueKey, PersistentDataType.DOUBLE);
        if (unique.isEmpty()) {
            return;
        }

        applyMeta(item.getItemStack(), uniqueKey, PersistentDataType.DOUBLE, 0D);
    }

    // coin glow

    /// the glow color is stored as an RGB int
    public void setGlowProperty(ItemMeta meta, NamedTextColor color) {
        applyMeta(meta, glowKey, PersistentDataType.INTEGER, color.value());
    }

    private Team getOrCreateTeam(String name) {
        var team = scoreboard.getTeam(name);
        return team == null? scoreboard.registerNewTeam(name) : team;
    }

    public void applyGlowIfPresent(Item item) {
        Optional<Integer> color = getMeta(item, glowKey, PersistentDataType.INTEGER);
        if (color.isEmpty()) {
            return;
        }

        NamedTextColor namedTextColor = NamedTextColor.nearestTo(TextColor.color(color.get()));
        Team team = getOrCreateTeam(TEAM_PREFIX + namedTextColor.asHexString().substring(1));
        coins.getComponentApi().setTeamColor(team, namedTextColor);
        team.addEntry(item.getUniqueId().toString());
        item.setGlowing(true);
    }

    // coin hologram

    public void setHologramProperty(ItemMeta meta, boolean value) {
        if (value) {
            applyMeta(meta, hologramKey, PersistentDataType.BOOLEAN, true);
        }
        else if (meta != null) {
            meta.getPersistentDataContainer().remove(hologramKey);
        }
    }

    public void applyHologramIfPresent(Item item) {
        Optional<Boolean> value = getMeta(item, hologramKey, PersistentDataType.BOOLEAN);
        if (value.isEmpty() || !value.get()) {
            return;
        }

        item.setCustomNameVisible(true);
        coins.getComponentApi().applyDisplayName(item);
    }

    // hopper pickup

    public void setNoHopperPickupProperty(ItemMeta meta, boolean value) {
        if (value) {
            applyMeta(meta, noHopperKey, PersistentDataType.BOOLEAN, true);
        }
        else if (meta != null) {
            meta.getPersistentDataContainer().remove(noHopperKey);
        }
    }

    public boolean isNoHopperPickup(Item item) {
        return getMeta(item, noHopperKey, PersistentDataType.BOOLEAN).isPresent();
    }

    // coin immutable name

    public void setImmutableProperty(ItemMeta meta, boolean value) {
        if (value) {
            applyMeta(meta, immutableKey, PersistentDataType.BOOLEAN, true);
        }
        else if (meta != null) {
            meta.getPersistentDataContainer().remove(immutableKey);
        }
    }

    public boolean isImmutableName(ItemStack item) {
        return getMeta(item, immutableKey, PersistentDataType.BOOLEAN).isPresent();
    }

    // coin pickup sound

    public void setSoundProperty(ItemMeta meta, @NotNull String sound, double volume, double pitch) {
        applyMeta(meta, soundKey, PersistentDataType.STRING, sound.toLowerCase());
        applyMeta(meta, volumeKey, PersistentDataType.FLOAT, (float) volume);
        applyMeta(meta, pitchKey, PersistentDataType.FLOAT, (float) pitch);
    }

    /// @return true if a sound was found on the item, it was then attempted to play
    @NullMarked
    public boolean playSound(Player player, ItemStack coin) {
        Optional<String> sound = getMeta(coin, soundKey, PersistentDataType.STRING);
        if (sound.isEmpty()) {
            return false;
        }

        player.playSound(
            player.getLocation(),
            sound.get(),
            getMeta(coin, volumeKey, PersistentDataType.FLOAT).orElse(1F),
            getMeta(coin, pitchKey, PersistentDataType.FLOAT).orElse(1F)
        );
        return true;
    }
}
