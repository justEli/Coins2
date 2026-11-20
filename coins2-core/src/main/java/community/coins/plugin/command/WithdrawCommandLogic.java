package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.coin.DefinedCoin;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.language.Language;
import community.coins.plugin.util.ComponentUtil;
import community.coins.plugin.util.Filler;
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
public abstract class WithdrawCommandLogic extends CommandLogic {
    public WithdrawCommandLogic(CoinsCore coins, DefinedCommand defined) {
        super(coins, defined);
    }

    private final Set<UUID> disabledWorldUuids = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean allowDecimals = new AtomicBoolean(false);
    private final AtomicReference<Component> itemName =  new AtomicReference<>();
    private final TreeMap<Long, DefinedCoin> coinValues = new TreeMap<>();
    private final Set<Action> depositActions = ConcurrentHashMap.newKeySet();

    @Override
    public boolean parseConfig(ConfigWarns.Named warns, ConfigurationSection section, ConfigService service) {
        // disabled-worlds
        disabledWorldUuids.clear();
        for (String worldName : section.getStringList("disabled-worlds")) {
            var world = coins.getAttributes().getWorld(worldName);
            if (world == null) {
                warns.warn("World '%s' to disable for command '%s' does not exist.".formatted(worldName, defined.getName()));
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
            warns.warn("Cannot register command '%s' because there are no coins defined to give.".formatted(defined.getName()));
            return false;
        }

        coinValues.clear();
        for (String coinName : coinsSection.getKeys(false)) {
            Optional<DefinedCoin> definedCoin = service.getCoinsConfig().getDefined(coinName);
            if (definedCoin.isEmpty()) {
                warns.warn("Cannot add coin '%s' for command '%s' because coin is not defined.".formatted(coinName, defined.getName()));
                continue;
            }

            long maxWithdrawValue = coinsSection.getLong(coinName);
            coinValues.put(maxWithdrawValue, definedCoin.get());
        }

        if (coinValues.isEmpty()) {
            warns.warn("Cannot register command '%s' because there are no coins defined to give.".formatted(defined.getName()));
            return false;
        }

        // deposit-actions
        depositActions.clear();
        for (String actionString : section.getStringList("deposit-actions")) {
            try {
                Action action = Action.valueOf(actionString.toUpperCase());
                depositActions.add(action);
            }
            catch (IllegalArgumentException _) {
                warns.warn("Deposit action '%s' to use for command '%s' does not exist.".formatted(actionString, defined.getName()));
            }
        }

        return true;
    }

    public void sendHelpMessage(CommandSender sender) {
        coins.sendMessage(sender, Language.COMMAND_INCOMPLETE.with(
            Filler.COMMAND.filled("/withdraw (%s) (%s)".formatted(Language.WORD_VALUE, Language.WORD_AMOUNT))
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
                Filler.TYPE.filled(Language.WORD_VALUE), Filler.MIN_1, Filler.MAX.filled(max)
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
                Filler.TYPE.filled(Language.WORD_AMOUNT), Filler.MIN_1, Filler.MAX.filled(64)
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

        // always allow hopper pickup for withdrawn coins
        coins.getCoinMeta().setNoHopperPickupProperty(meta, false);
        coins.getCoinMeta().setDepositActions(meta, depositActions);

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
                    Filler.AMOUNT.filled(amount), Filler.COIN.filled(name), Filler.PRICE.filled(currency.getFormatMessage(cost))
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
