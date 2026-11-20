package community.coins.plugin.economy.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import community.coins.plugin.CoinsCore;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Eli
 * @since November 19, 2026
 */
public final class SqlConnection {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    static {
        EXECUTOR.submit(() -> Thread.currentThread().setName("async-sql-io-thread"));
    }

    private final CoinsCore coins;
    private HikariDataSource hikari; // null if not configured

    /// initializes a sql handler without an active connection
    public SqlConnection(CoinsCore coins) {
        this.coins = coins;

        coins.addShutdownTask(() -> {
            closeConnection(); // submit to sql thread that connection must be closed
            try {
                // start closing the thread and wait 10 seconds at most for tasks to finish and connection to close
                EXECUTOR.shutdown();
                if (!EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                    EXECUTOR.shutdownNow();
                }
            }
            catch (InterruptedException _) {
                EXECUTOR.shutdownNow();
            }
        });
    }

    public void openConnection(HikariConfig hikariConfig) {
        closeConnection();
        EXECUTOR.submit(() -> {
            try {
                this.hikari = new HikariDataSource(hikariConfig);
            }
            catch (Exception exception) {
                StringBuilder message = new StringBuilder("Failed to establish SQL connection.");
                message.append(" ").append(exception.getMessage());

                Throwable cause = exception.getCause();
                if (cause != null && !exception.getMessage().contains(cause.getMessage())) {
                    message.append(" ").append(cause.getMessage());
                }

                coins.log(Level.SEVERE, message.toString().replace("\n", " "));
            }
        });

        // queue it to the executor right after a connection is attempted to be opened
        //  so that it is the very first thing to execute after connection opening
        createCoinBalancesTable();
    }

    private void createCoinBalancesTable() {
        query("""
            CREATE TABLE IF NOT EXISTS coin_balances(
              uuid CHAR(36) NOT NULL,
              currency VARCHAR(50) NOT NULL,
              balance DECIMAL(19, 6) NOT NULL DEFAULT 0,
              queued_balance DECIMAL(19, 6) NOT NULL DEFAULT 0,
              received_inactive DECIMAL(19, 6) NOT NULL DEFAULT 0,
              PRIMARY KEY (uuid, currency),
              INDEX (currency),
              INDEX (balance),
              INDEX (queued_balance),
              INDEX (received_inactive)
            )"""
        ).bind().executeUpdate();
    }

    /// submits to the single threaded sql executor that the connection must be closed
    public void closeConnection() {
        EXECUTOR.submit(() -> {
            if (hikari != null && !hikari.isClosed()) {
                hikari.close();
                // todo set this.hikari = null?
            }
        });
    }

    /// gets an SQL connection from the pool
    public @Nullable Connection getConnection() throws SQLException {
        if (hikari == null) {
            return null;
        }

        return hikari.getConnection();
    }

    public Query query(@Language("SQL") String query) {
        return new Query(query);
    }

    public final class Query {
        private final String query;
        public Query(String query) {
            this.query = query;
        }

        public BoundQuery bind(Object... replacements) {
            return new BoundQuery(query, replacements);
        }
    }

    public final class BoundQuery {
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
            EXECUTOR.submit(() -> {
                try (var connection = getConnection()) {
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
                    coins.log(Level.WARNING, "Failed to execute query (%s):\n%s".formatted(
                        exception.getMessage(), query.replaceAll("(?m)^", "    ")
                    ));
                }
            });
        }

        public void executeUpdate() {
            EXECUTOR.submit(() -> {
                try (var connection = getConnection()) {
                    if (connection == null) {
                        throw new SQLException("Cannot get connection from pool");
                    }

                    try (var statement = connection.prepareStatement(query)) {
                        parseReplacements(statement, replacements);
                        statement.executeUpdate();
                    }
                }
                catch (SQLException exception) {
                    coins.log(Level.WARNING, "Failed to execute query (%s):\n%s".formatted(
                        exception.getMessage(), query.replaceAll("(?m)^", "    ")
                    ));
                }
            });
        }
    }

    @FunctionalInterface
    public interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}
