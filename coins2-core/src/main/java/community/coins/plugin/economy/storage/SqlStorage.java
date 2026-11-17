package community.coins.plugin.economy.storage;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.CurrenciesConfig;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @author Eli
 * @since May 20, 2026
 */
public final class SqlStorage implements CurrencyStorage {
    private final CoinsCore coins;
    private final CurrenciesConfig config;
    private final String currencyIdentifier;

    public SqlStorage(CoinsCore coins, ConfigService service, String currencyIdentifier) {
        this.coins = coins;
        this.config = service.getCurrenciesConfig();
        this.currencyIdentifier = currencyIdentifier;

        new Query("""
            CREATE TABLE IF NOT EXISTS coin_balances(\
            uuid CHAR(36) NOT NULL,\
            currency VARCHAR(50) NOT NULL,\
            balance DECIMAL(19, 6) NOT NULL DEFAULT 0,\
            queued_balance DECIMAL(19, 6) NOT NULL DEFAULT 0,\
            received_inactive DECIMAL(19, 6) NOT NULL DEFAULT 0,\
            PRIMARY KEY(uuid, currency),\
            INDEX(currency),\
            INDEX(balance),\
            INDEX(queued_balance),\
            INDEX(received_inactive)\
            )"""
        ).bind().executeUpdate();
    }

    private final Query loadBalancesQuery =
        new Query("SELECT uuid, balance, received_inactive FROM coin_balances WHERE currency = ?");

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

    private final Query createAccountQuery =
        new Query("INSERT INTO coin_balances (uuid, balance, currency) VALUES (?, ?, ?)");

    @Override
    public void createAccount(UUID uuid, double amount) {
        createAccountQuery.bind(uuid, amount, currencyIdentifier).executeUpdate();
    }

    private final Query withdrawQuery = new Query("""
        INSERT INTO coin_balances (uuid, currency, balance)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE balance = balance - ?"""
    );

    @Override
    public void withdraw(UUID uuid, double amount) {
        withdrawQuery.bind(uuid, currencyIdentifier, -amount, amount).executeUpdate();
    }

    private final Query depositQuery = new Query("""
        INSERT INTO coin_balances (uuid, currency, balance)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE balance = balance + ?"""
    );

    @Override
    public void deposit(UUID uuid, double amount) {
        depositQuery.bind(uuid, currencyIdentifier, amount, amount).executeUpdate();
    }

    private final Query addInactiveCoinsQuery = new Query("""
        INSERT INTO coin_balances (uuid, currency, received_inactive)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE received_inactive = received_inactive + ?""");

    @Override
    public void addInactiveCoins(UUID uuid, double amount) {
        addInactiveCoinsQuery.bind(uuid, currencyIdentifier, amount, amount).executeUpdate();
    }

    private final Query resetInactiveCoinsQuery = new Query("""
        INSERT INTO coin_balances (uuid, currency, received_inactive)
        VALUES (?, ?, 0)
        ON DUPLICATE KEY UPDATE received_inactive = 0""");

    @Override
    public void resetInactiveCoins(UUID uuid) {
        resetInactiveCoinsQuery.bind(uuid, currencyIdentifier).executeUpdate();
    }

    private final class Query {
        private final String query;
        public Query(@Language("SQL") String query) {
            this.query = query;
        }

        public BoundQuery bind(Object... replacements) {
            return new BoundQuery(query, replacements);
        }
    }

    private final class BoundQuery {
        private final String query;
        private final Object[] replacements;

        public BoundQuery(String query, Object[] replacements) {
            this.query = query;
            this.replacements = replacements;
        }

        private static void parseReplacements(PreparedStatement statement, Object... replacements) throws SQLException {
            int i = 1;
            for (Object replacement : replacements) {
                if (replacement instanceof Optional<?> optional) {
                    replacement = optional.orElse(null);
                }
                else if (replacement instanceof UUID uuid) {
                    replacement = uuid.toString();
                }

                if (replacement == null) {
                    statement.setNull(i++, Types.NULL);
                }
                else {
                    statement.setObject(i++, replacement);
                }
            }
        }

        public void executeQuery(SqlConsumer<ResultSet> data) {
            CurrenciesConfig.SQL_EXECUTOR.submit(() -> {
                try (var connection = config.getConnection()) {
                    if (connection == null) {
                        throw new SQLException("Cannot get connection from pool");
                    }

                    try (var statement = connection.prepareStatement(query)) {
                        parseReplacements(statement, replacements);
                        try (var resultSet = statement.executeQuery()) {
                            data.accept(resultSet);
                        }
                    }
                }
                catch (SQLException exception) {
                    coins.log(Level.WARNING, "Failed to execute query ('%s'): %s".formatted(query, exception.getMessage()));
                }
            });
        }

        public void executeUpdate() {
            CurrenciesConfig.SQL_EXECUTOR.submit(() -> {
                try (var connection = config.getConnection()) {
                    if (connection == null) {
                        throw new SQLException("Cannot get connection from pool");
                    }

                    try (var statement = connection.prepareStatement(query)) {
                        parseReplacements(statement, replacements);
                        statement.executeUpdate();
                    }
                }
                catch (SQLException exception) {
                    coins.log(Level.WARNING, "Failed to execute query ('%s'): %s".formatted(query, exception.getMessage()));
                }
            });
        }
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
