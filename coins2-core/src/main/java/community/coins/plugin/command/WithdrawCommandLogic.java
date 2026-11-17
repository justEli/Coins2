package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.coin.DefinedCoin;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.language.EntryReplacement;
import community.coins.plugin.language.Language;
import community.coins.plugin.util.ComponentUtil;
import community.coins.plugin.util.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eli
 * @since November 15, 2026
 */
public abstract class WithdrawCommandLogic<T extends CoinsCore> extends CommandLogic<T> {
    public WithdrawCommandLogic(T coins, CommandService service) {
        super(coins, service, "withdraw");
    }

    protected static final EntryReplacement FILL_AMOUNT = new EntryReplacement("amount");
    protected static final EntryReplacement FILL_TYPE = new EntryReplacement("type");
    private static final EntryReplacement.Filled FILL_MIN_1 = new EntryReplacement("min").filled(1);
    private static final EntryReplacement FILL_MAX = new EntryReplacement("max");
    private static final EntryReplacement FILL_COIN = new EntryReplacement("coin");
    private static final EntryReplacement FILL_PRICE = new EntryReplacement("price");

    private final Set<UUID> disabledWorldUuids = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean allowDecimals = new AtomicBoolean(false);
    private final AtomicReference<Component> itemName =  new AtomicReference<>();
    private final TreeMap<Long, DefinedCoin> coinValues = new TreeMap<>();

    @Override
    public boolean parseConfig(ConfigWarns.Named warns, ConfigurationSection section, ConfigService service) {
        // disabled-worlds
        disabledWorldUuids.clear();
        for (String worldName : section.getStringList("disabled-worlds")) {
            var world = coins.getAttributes().getWorld(worldName);
            if (world == null) {
                warns.warn("World '%s' to disable for command '%s' does not exist.".formatted(worldName, identifier));
                continue;
            }
            disabledWorldUuids.add(world.getUID());
        }

        // allow-decimals
        allowDecimals.set(section.getBoolean("allow-decimals"));

        // item-name
        itemName.set(ComponentUtil.parse(section.getString("item-name", "<var>{value} {coin.name}")));

        // coins
        ConfigurationSection coinsSection = section.getConfigurationSection("coins");
        if (coinsSection == null) {
            warns.warn("Cannot register command '%s' because there are no coins defined to give.".formatted(identifier));
            return false;
        }

        coinValues.clear();
        for (String coinName : coinsSection.getKeys(false)) {
            Optional<DefinedCoin> definedCoin = service.getCoinsConfig().getDefinedItem(coinName);
            if (definedCoin.isEmpty()) {
                warns.warn("Cannot add coin '%s' for command '%s' because coin is not defined.".formatted(coinName, identifier));
                continue;
            }

            long maxWithdrawValue = coinsSection.getLong(coinName);
            coinValues.put(maxWithdrawValue, definedCoin.get());
        }

        if (coinValues.isEmpty()) {
            warns.warn("Cannot register command '%s' because there are no coins defined to give.".formatted(identifier));
            return false;
        }

        // deposit-actions
        coins.getGlobalState().getDepositActions().clear();
        for (String actionString : section.getStringList("deposit-actions")) {
            try {
                Action action = Action.valueOf(actionString.toUpperCase());
                coins.getGlobalState().getDepositActions().add(action);
            }
            catch (IllegalArgumentException _) {
                warns.warn("Deposit action '%s' to use for command '%s' does not exist.".formatted(actionString, identifier));
            }
        }

        return true;
    }

    private static final EntryReplacement FILL_COMMAND = new EntryReplacement("command");

    public void sendHelpMessage(CommandSender sender) {
        coins.sendMessage(sender, Language.COMMAND_INCOMPLETE.with(
            FILL_COMMAND.filled("/withdraw (%s) (%s)".formatted(Language.WORD_VALUE, Language.WORD_AMOUNT))
        ));
    }

    public void withdrawCoins(CommandSender sender, double value0, int amount) {
        if (!(sender instanceof Player player)) {
            coins.sendMessage(sender,  Language.PLAYERS_ONLY);
            return;
        }

        if (disabledWorldUuids.contains(player.getWorld().getUID())) {
            coins.sendMessage(sender, Language.DISABLED_WORLD);
            return;
        }

        long roundedValue = (long) value0;
        var entry = coinValues.ceilingEntry(roundedValue);

        if (entry == null || roundedValue <= 0) {
            long max = coinValues.lastKey(); // get the highest entry, which is the limit
            coins.sendMessage(sender, Language.COMMAND_INVALID_RANGE.with(
                FILL_TYPE.filled(Language.WORD_VALUE), FILL_MIN_1, FILL_MAX.filled(max)
            ));
            return;
        }

        DefinedCoin coin = entry.getValue();
        DefinedCurrency currency = coin.getCurrency();
        double value = allowDecimals.get()? Util.toRoundedMoneyDecimals(value0, currency.getDecimals()) : roundedValue;

        ItemStack item = coin.getItemStackClone();
        ItemMeta meta = item.getItemMeta();

        if (meta == null || amount > 64) {
            coins.sendMessage(sender, Language.COMMAND_INVALID_RANGE.with(
                FILL_TYPE.filled(Language.WORD_AMOUNT), FILL_MIN_1, FILL_MAX.filled(64)
            ));
            return;
        }

        Component name = itemName.get()
            .replaceText(c -> c.matchLiteral("{value}").replacement(allowDecimals.get()? currency.getDecimalFormatted(value) : Util.withSeparators(roundedValue)))
            .replaceText(c -> c.matchLiteral("{coin.name}").replacement(value == 1D? coin.getSingularName() : coin.getPluralName()))
            .replaceText(c -> c.matchLiteral("{currency.name}").replacement(value == 1D? currency.getSingularName() : currency.getPluralName()));

        coins.getComponentApi().setDisplayName(meta, name);
        coins.getCoinMeta().setCoinValue(meta, value);
        coins.getCoinMeta().setWithdrawOwner(meta, player.getUniqueId());
        coins.getCoinMeta().setNoHopperPickupProperty(meta, false); // always allow hopper pickup for withdrawn coins

        item.setItemMeta(meta);
        item.setAmount(amount);

        double cost = BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(amount)).doubleValue();
        if (cost == Double.NEGATIVE_INFINITY || cost == Double.POSITIVE_INFINITY) {
            coins.sendMessage(sender, Language.WITHDRAW_MAX_AMOUNT);
            return;
        }

        currency.submitTransaction(transaction -> {
            if (!transaction.canAfford(player.getUniqueId(), cost)) {
                coins.sendMessage(sender, Language.WITHDRAW_MAX_AMOUNT);
                return;
            }

            if (transaction.withdraw(player.getUniqueId(), cost)) {
                coins.getScheduler().runEntityTask(player, () -> player.getInventory().addItem(item));
                coins.sendMessage(sender, Language.WITHDRAW_GIVE.with(
                    FILL_AMOUNT.filled(amount), FILL_COIN.filled(name), FILL_PRICE.filled(currency.getFormatMessage(cost))
                ));
            }
            else {
                coins.sendMessage(sender, Language.TRANSACTION_FAIL);
            }
        });
    }

    @Override
    public String getDescription() {
        return "Command to withdraw virtual money into physical coins.";
    }
}
