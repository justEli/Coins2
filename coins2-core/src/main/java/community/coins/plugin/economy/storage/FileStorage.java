package community.coins.plugin.economy.storage;

import community.coins.plugin.CoinsCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static community.coins.plugin.economy.storage.BalanceData.rounded;

/**
 * @author Eli
 * @since May 20, 2026
 */
public final class FileStorage implements CurrencyStorage {
    private final Path storagePath;
    private final Map<UUID, FileConfiguration> cachedContents = new ConcurrentHashMap<>();

    public FileStorage(CoinsCore coins, String currencyIdentifier) {
        this.storagePath = coins.getDataFolder().toPath().resolve("storage").resolve("balances").resolve(currencyIdentifier);
        try {
            // create directories
            Files.createDirectories(storagePath);
        }
        catch (IOException _) {}
    }

    // queue for submitting file changes
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    static {
        EXECUTOR_SERVICE.submit(() -> Thread.currentThread().setName("async-file-io-thread"));
    }

    // run from the executor
    private void saveFile(FileConfiguration data, Path file) {
        try {
            if (!Files.isRegularFile(file)) { // create file if it doesn't exist
                Files.createDirectories(file.toAbsolutePath().getParent());
                Files.createFile(file);
            }

            // write to the file
            Files.writeString(file, data.saveToString());
        }
        catch (Exception _) {}
    }

    @Override
    public void loadBalances(Consumer<Collection<BalanceData>> balances) {
        List<BalanceData> balanceData = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(storagePath)) {
            for (Path file : stream.filter(path -> path.toString().endsWith(".yml")).toList()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());

                String rawUuid = config.getString("uuid");
                if (rawUuid == null) {
                    continue; // not a valid file
                }

                UUID uuid = UUID.fromString(rawUuid);
                balanceData.add(
                    new BalanceData(uuid, config.getDouble("balance", 0D), config.getDouble("received-inactive", 0D))
                );
                cachedContents.put(uuid, config);
            }
        }
        catch (IOException _) {}

        balances.accept(balanceData);
    }

    private Path getFile(UUID uuid) {
        return storagePath.resolve("%s.yml".formatted(uuid.toString()));
    }

    @Override
    public void createAccount(UUID uuid, double amount) {
        EXECUTOR_SERVICE.submit(() -> {
            if (cachedContents.containsKey(uuid)) {
                return; // account already exists
            }

            try {
                Path file = getFile(uuid);
                Files.createFile(file); // throws if already exists

                FileConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());
                config.set("uuid", uuid.toString());
                config.set("balance", amount);

                // todo call event that data was created

                cachedContents.put(uuid, config);
                saveFile(config, file);
            }
            catch (IOException _) {}
        });
    }

    public void modifyFile(UUID uuid, String key, Function<Double, Double> amount) {
        EXECUTOR_SERVICE.submit(() -> {
            FileConfiguration config = cachedContents.get(uuid);
            if (config == null) {
                return; // todo create file
            }

            double current = config.getDouble(key);
            config.set(key, rounded(amount.apply(current)));

            cachedContents.put(uuid, config);
            saveFile(config, getFile(uuid));
        });
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
        modifyFile(uuid, "balance", current -> current - amount);
    }

    @Override
    public void deposit(UUID uuid, double amount) {
        modifyFile(uuid, "balance", current -> current + amount);
    }

    @Override
    public void addInactiveCoins(UUID uuid, double amount) {
        modifyFile(uuid, "received-inactive", current -> current + amount);
    }

    @Override
    public void resetInactiveCoins(UUID uuid) {
        modifyFile(uuid, "received-inactive", _ -> 0D);
    }
}
