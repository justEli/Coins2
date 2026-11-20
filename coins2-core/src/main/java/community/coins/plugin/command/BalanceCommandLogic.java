package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.language.EntryReplacement;
import community.coins.plugin.language.Language;
import community.coins.plugin.util.BasicPlayer;
import community.coins.plugin.util.ColorResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eli
 * @since May 21, 2026
 */
public abstract class BalanceCommandLogic extends CommandLogic {
    public BalanceCommandLogic(CoinsCore coins, DefinedCommand defined) {
        super(coins, defined);
    }

    protected final AtomicReference<String> targetPermission = new AtomicReference<>();
    protected final AtomicReference<DefinedCurrency> definedCurrency = new AtomicReference<>();

    private static final EntryReplacement FILL_ID = new EntryReplacement("identifier");
    private static final EntryReplacement FILL_CURRENCY = new EntryReplacement("currency");
    private static final EntryReplacement FILL_BALANCE = new EntryReplacement("balance");
    private static final EntryReplacement FILL_USERNAME = new EntryReplacement("username", ColorResolver.USER);

    /// @param currencyId can also be null if the currency was configured in the config
    public void showBalance(CommandSender sender, @Nullable String currencyId, @Nullable BasicPlayer target) {
        if (definedCurrency.get() != null) {
            showBalance(sender, definedCurrency.get(), target);
            return;
        }

        if (currencyId == null) {
            if (coins.getConfigService().getCurrenciesConfig().getDefinedKeys().size() != 1) {
                coins.sendMessage(sender, Language.BALANCE_PROVIDE_CURRENCY);
                return;
            }

            showBalance(sender, coins.getConfigService().getCurrenciesConfig().getDefinedValues().iterator().next(), target);
            return;
        }

        Optional<DefinedCurrency> currency = coins.getConfigService().getCurrenciesConfig().getDefined(currencyId);
        if (currency.isEmpty()) {
            coins.sendMessage(sender, Language.CURRENCY_NOT_FOUND.with(FILL_ID.filled(currencyId)));
            return;
        }

        showBalance(sender, currency.get(), target);
    }

    private void showBalance(CommandSender sender, @NotNull DefinedCurrency currency, @Nullable BasicPlayer target) {
        String username;
        UUID uuid;

        if (target == null) {
            if (!(sender instanceof Player player)) {
                coins.sendMessage(sender, Language.BALANCE_PROVIDE_PLAYER);
                return;
            }
            username = player.getName();
            uuid = player.getUniqueId();
        }
        else {
            username = target.getUsername();
            uuid = target.getUuid();
        }

        if (username == null || uuid == null) {
            coins.sendMessage(sender, Language.INVALID_PLAYER);
            return;
        }

        currency.submitTransaction(transaction -> {
            if (sender.getName().equalsIgnoreCase(username)) {
                coins.sendMessage(sender, Language.BALANCE_DISPLAY.with(
                    FILL_CURRENCY.filled(currency.getPluralName()),
                    FILL_BALANCE.filled(currency.getFormatMessage(transaction.getBalance(uuid)))
                ));
            }
            else {
                coins.sendMessage(sender, Language.BALANCE_DISPLAY_OTHERS.with(
                    FILL_USERNAME.filled(username),
                    FILL_CURRENCY.filled(currency.getPluralName()),
                    FILL_BALANCE.filled(currency.getFormatMessage(transaction.getBalance(uuid)))
                ));
            }
        });
    }

    @Override
    public String getDescription() {
        return "Show your current balance for a specific currency.";
    }

    @Override
    public boolean parseConfig(ConfigWarns.Named warns, ConfigurationSection section, ConfigService service) {
        // this doesn't work after reload for Paper
        targetPermission.set(section.getString("target-permission", "coins.command.balance.others"));

        String currencyName = section.getString("currency");
        if (currencyName != null) {
            Optional<DefinedCurrency> currency = service.getCurrenciesConfig().getDefined(currencyName);
            if (currency.isEmpty()) {
                warns.warn("Cannot register command '%s' because currency '%s' does not exist.".formatted(defined.getName(), currencyName));
                return false;
            }

            definedCurrency.set(currency.get());
        }
        return true;
    }
}
