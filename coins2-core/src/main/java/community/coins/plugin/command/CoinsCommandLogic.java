package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.language.EntryReplacement;
import community.coins.plugin.language.Language;
import community.coins.plugin.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

/**
 * @author Eli
 * @since May 07, 2026
 */
public abstract class CoinsCommandLogic extends CommandLogic {
    public CoinsCommandLogic(CoinsCore coins, CommandService service) {
        super(coins, service, "coins");
    }

    @Override
    public String getDescription() {
        return "Command with tools for coins.";
    }

    private static final EntryReplacement FILL_DURATION = new EntryReplacement("duration");
    private static final EntryReplacement FILL_ID = new EntryReplacement("identifier");
    private static final EntryReplacement FILL_FORMAT = new EntryReplacement("format");

    public void reload(CommandSender sender) {
        long millis = System.currentTimeMillis();
        coins.getConfigService().reload();
        long duration = System.currentTimeMillis() - millis;

        coins.sendMessage(sender, Language.RELOAD_SUCCESS.with(FILL_DURATION.filled(duration)));
    }

    public void giveCoin(CommandSender sender, String coinIdentifier) {
        if (!(sender instanceof Player player)) {
            coins.sendMessage(sender,  Language.PLAYERS_ONLY);
            return;
        }

        var coin = coins.getConfigService().getCoinsConfig().getDefinedItem(coinIdentifier);
        if (coin.isEmpty()) {
            coins.sendMessage(sender,  Language.COIN_NOT_FOUND.with(FILL_ID.filled(coinIdentifier)));
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            coins.sendMessage(sender, Language.FULL_INVENTORY);
            return;
        }

        player.getInventory().addItem(coin.get().getItemStackClone());
        coins.sendMessage(sender, Language.GIVE_SUCCESS.with(FILL_ID.filled(coinIdentifier)));
    }

    public void setCoinValue(CommandSender sender, String currencyName, double value) {
        if (!(sender instanceof Player player)) {
            coins.sendMessage(sender, Language.PLAYERS_ONLY);
            return;
        }

        ItemStack stack = player.getInventory().getItemInMainHand();
        if (coins.getCoinMeta().getCoinCurrency(stack).isEmpty()) {
            coins.sendMessage(sender, Language.HOLD_A_COIN);
            return;
        }

        Optional<DefinedCurrency> currency = coins.getEconomyService().getCurrency(currencyName);
        if (currency.isEmpty()) {
            coins.sendMessage(sender, Language.CURRENCY_NOT_FOUND.with(FILL_ID.filled(currencyName)));
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        coins.getCoinMeta().setCoinCurrency(meta, currency.get());
        coins.getCoinMeta().setCoinValue(meta, Util.toRoundedMoneyDecimals(value, currency.get().getDecimals()));
        stack.setItemMeta(meta);

        coins.sendMessage(
            sender,
            Language.SET_VALUE_SUCCESS.with(FILL_FORMAT.filled(currency.get().getFormatMessage(value)))
        );
    }
}
