package community.coins.plugin.economy.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Eli
 * @since May 20, 2026
 */
public final class SqlStorage implements CurrencyStorage {
    private final String currencyIdentifier;

    public SqlStorage(SqlConnection sql, String currencyIdentifier) {
        this.currencyIdentifier = currencyIdentifier;

        this.loadBalancesQuery = sql.query("SELECT uuid, balance, received_inactive FROM coin_balances WHERE currency = ?");
        this.createAccountQuery = sql.query("INSERT INTO coin_balances (uuid, balance, currency) VALUES (?, ?, ?)");

        this.withdrawQuery = sql.query("""
            INSERT INTO coin_balances (uuid, currency, balance)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE balance = balance - ?"""
        );

        this.depositQuery = sql.query("""
            INSERT INTO coin_balances (uuid, currency, balance)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE balance = balance + ?"""
        );

        this.addInactiveCoinsQuery = sql.query("""
            INSERT INTO coin_balances (uuid, currency, received_inactive)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE received_inactive = received_inactive + ?"""
        );

        this.resetInactiveCoinsQuery = sql.query("""
            INSERT INTO coin_balances (uuid, currency, received_inactive)
            VALUES (?, ?, 0)
            ON DUPLICATE KEY UPDATE received_inactive = 0"""
        );
    }

    private final SqlConnection.Query loadBalancesQuery;

    @Override
    public void loadBalances(Consumer<Collection<BalanceData>> balances) {
        loadBalancesQuery.bind(currencyIdentifier).executeQuery(data -> {
            List<BalanceData> balanceData = new ArrayList<>();
            while (data.next()) {
                String rawUuid = data.getString("uuid");
                if (rawUuid == null) {
                    continue;
                }

                UUID uuid = UUID.fromString(rawUuid);
                balanceData.add(new BalanceData(uuid, data.getDouble("balance"), data.getDouble("received_inactive")));
            }
            balances.accept(balanceData);
        });
    }

    private final SqlConnection.Query createAccountQuery;

    @Override
    public void createAccount(UUID uuid, double amount) {
        createAccountQuery.bind(uuid, amount, currencyIdentifier).executeUpdate();
    }

    private final SqlConnection.Query withdrawQuery;

    @Override
    public void withdraw(UUID uuid, double amount) {
        withdrawQuery.bind(uuid, currencyIdentifier, -amount, amount).executeUpdate();
    }

    private final SqlConnection.Query depositQuery;

    @Override
    public void deposit(UUID uuid, double amount) {
        depositQuery.bind(uuid, currencyIdentifier, amount, amount).executeUpdate();
    }

    private final SqlConnection.Query addInactiveCoinsQuery;

    @Override
    public void addInactiveCoins(UUID uuid, double amount) {
        addInactiveCoinsQuery.bind(uuid, currencyIdentifier, amount, amount).executeUpdate();
    }

    private final SqlConnection.Query resetInactiveCoinsQuery;

    @Override
    public void resetInactiveCoins(UUID uuid) {
        resetInactiveCoinsQuery.bind(uuid, currencyIdentifier).executeUpdate();
    }
}
