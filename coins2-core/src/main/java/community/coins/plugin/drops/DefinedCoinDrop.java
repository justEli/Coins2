package community.coins.plugin.drops;

import community.coins.plugin.config.ConfigService;
import community.coins.plugin.config.ConfigWarns;
import community.coins.plugin.coin.DefinedCoin;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.TreeMap;

/**
 * handles mostly the 'coins' section in the drops.yml config
 * @author Eli
 * @since May 04, 2026
 */
@NullMarked
public final class DefinedCoinDrop {
    private final NavigableMap<Double, AmountedCoin> coinChances = new TreeMap<>();
    private static final SplittableRandom RANDOM = new SplittableRandom();

    /// @param section config section 'coins' within the configured drop
    /// @param identifier identifier of the drop, already formatted using {@code Util#toIdentifier}
    public DefinedCoinDrop(ConfigService service, ConfigWarns.Named warns, ConfigurationSection section, String identifier) {
        double total = 0;
        for (String coinName : section.getKeys(false)) {
            Optional<DefinedCoin> definedCoin = service.getCoinsConfig().getDefined(coinName);
            if (definedCoin.isEmpty()) {
                warns.warn("Cannot add coin '%s' for drop '%s' because coin is not defined.".formatted(coinName, identifier));
                continue;
            }

            ConfigurationSection coinSection = section.getConfigurationSection(coinName);
            if (coinSection == null) {
                continue; // should be impossible
            }

            double chance = coinSection.getDouble("chance", 1);
            if (chance <= 0 || chance > 1) {
                warns.warn("Cannot add coin '%s' for drop '%s' because chance is not between 0.00 and 1.00.".formatted(coinName, identifier));
                continue;
            }

            double min = coinSection.getDouble("value", -1);
            double max = coinSection.getDouble("value", -1);

            if (min < 0 || max < 0) {
                List<Double> values = coinSection.getDoubleList("value");
                if (values.size() == 2) {
                    min = Math.abs(values.get(0));
                    max = Math.abs(values.get(1));
                }
                else {
                    min = 1;
                    max = 1;
                }
            }

            // flip if max is lower than min
            if (min > max) {
                double minimum = min;
                min = max;
                max = minimum;
            }

            total += chance;

            int decimals = definedCoin.get().getCurrency().getDecimals();
            coinChances.put(total, new AmountedCoin(min, max, decimals, definedCoin.get()));
        }

        if (total > 1) {
            warns.warn("""
                Total coin drop chance for drop '%s' is higher than 1.00 (100%%), which can lead to unexpected drop behavior."""
                .formatted(identifier)
            );
        }
    }

    // coins:
    //   'coin_name':
    //     value: 1
    //     chance: 0.05
    //   'coins_two':
    //     value: [1, 5]
    //     chance: 0.40

    /// pick a random coin from the list based on the configured chance
    /// @return empty when the chance didn't allow it
    public Optional<AmountedCoin> getRandomPick() {
        var coin = coinChances.ceilingEntry(RANDOM.nextDouble());
        if (coin == null) {
            return Optional.empty(); // leftover probability
        }

        return Optional.of(coin.getValue());
    }
}
