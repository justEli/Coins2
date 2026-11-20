package community.coins.plugin.economy.hook;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.economy.DefinedCurrency;
import community.coins.plugin.economy.storage.CurrencyBalanceStorage;
import community.coins.plugin.event.BalanceChangeEvent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * @author Eli
 * @since May 19, 2026
 */
public final class VaultEconomyProvider implements Economy {
    private final CoinsCore coins;
    private final DefinedCurrency currency;

    // just a reference to the hook's balance cache and storage
    // its storage implementation (sql or file) can change on reload, but CurrencyBalanceStorage never changes
    private final CurrencyBalanceStorage storage;

    public VaultEconomyProvider(CoinsCore coins, DefinedCurrency currency) {
        this.coins = coins;
        this.currency = currency;

        // DefinedCurrency is recreated every reload, its EconomyHook isn't, so get the storage from there.
        // CurrencyBalanceStorage is never recreated for a specific currency, but its CurrencyStorage can be updated (recreated)
        this.storage = currency.getHook().getCurrencyStorage(currency);
    }

    @ApiStatus.AvailableSince("2.2")
    public CurrencyBalanceStorage getStorage() {
        return storage;
    }

    private static final EconomyResponse UNKNOWN_PLAYER =
        new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Unknown player");

    private static final EconomyResponse NOT_SUPPORTED =
        new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Not supported");

    private @Nullable UUID getUuid(@Nullable String username) {
        if (username == null) {
            return null;
        }

        var player = coins.getServer().getPlayerExact(username);
        if (player != null) {
            return player.getUniqueId();
        }

        // todo this should be avoided because of web requests
        return coins.getServer().getOfflinePlayer(username).getUniqueId();
    }

    @Override
    public boolean isEnabled() {
        return coins.isEnabled();
    }

    @Override
    public String getName() {
        return currency.getSingularName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return currency.getDecimals();
    }

    @Override
    public String format(double amount) {
        return currency.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return currency.getPluralName();
    }

    @Override
    public String currencyNameSingular() {
        return currency.getSingularName();
    }

    public boolean hasAccount(UUID uuid) {
        return storage.hasExistingBalance(uuid);
    }

    @Override
    public boolean hasAccount(String playerName) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return false;
        }

        return hasAccount(uuid);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        if (player == null) {
            return false;
        }

        return hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    public double getBalance(UUID uuid) {
        return storage.getCachedBalance(uuid);
    }

    @Override
    public double getBalance(String playerName) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return 0;
        }

        return getBalance(uuid);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player == null) {
            return 0;
        }

        return getBalance(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    public boolean has(UUID uuid, double amount) {
        return amount >= 0 && storage.getCachedBalance(uuid) >= amount;
    }

    @Override
    public boolean has(String playerName, double amount) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return false;
        }

        return has(uuid, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        if (player == null) {
            return false;
        }

        return has(player.getUniqueId(), amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    public EconomyResponse withdraw(UUID uuid, double amount) {
        double balance = storage.getCachedBalance(uuid);
        coins.debug("Withdraw: amount=%.2f, player=%s, previous_balance=%.2f".formatted(amount, uuid, balance));
        BalanceChangeEvent event = new BalanceChangeEvent(!coins.getServer().isPrimaryThread(), uuid, -amount, balance, currency);

        coins.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "Withdraw canceled");
        }

        storage.withdraw(uuid, amount);
        return new EconomyResponse(amount, storage.getCachedBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return UNKNOWN_PLAYER;
        }

        return withdraw(uuid, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return UNKNOWN_PLAYER;
        }

        return withdraw(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    public EconomyResponse deposit(UUID uuid, double amount) {
        double balance = storage.getCachedBalance(uuid);
        coins.debug("Deposit: amount=%.2f, player=%s, previous_balance=%.2f".formatted(amount, uuid, balance));
        BalanceChangeEvent event = new BalanceChangeEvent(!coins.getServer().isPrimaryThread(), uuid, amount, balance, currency);

        coins.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.FAILURE, "Deposit canceled");
        }

        storage.deposit(uuid, amount);
        return new EconomyResponse(amount, storage.getCachedBalance(uuid), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return UNKNOWN_PLAYER;
        }

        return deposit(uuid, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return UNKNOWN_PLAYER;
        }

        return deposit(player.getUniqueId(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return NOT_SUPPORTED;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        UUID uuid = getUuid(playerName);
        if (uuid == null) {
            return false;
        }

        storage.createIfNotExists(uuid);
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        if (player == null) {
            return false;
        }

        storage.createIfNotExists(player.getUniqueId());
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
}
