package community.coins.plugin.economy;

import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Eli
 * @since May 05, 2026
 */
@NullMarked
public abstract class EconomyHook implements EconomyAction {
    private final String name;
    public EconomyHook(EconomyService service, String name) {
        this.name = name; // not an identifier, but a case-sensitive (plugin) name

        // register this currency to Coins' economy service
        service.registerEconomy(this);
    }

    private final Map<String, DefinedCurrency> currencies = new HashMap<>();

    protected void addCurrency(DefinedCurrency currency) {
        currencies.put(currency.getIdentifier(), currency);
    }

    protected Optional<DefinedCurrency> getCurrency(String currency) {
        return Optional.ofNullable(currencies.get(currency.toLowerCase()));
    }

    protected int getAmountOfCurrencies() {
        return currencies.size();
    }

    protected void clearCurrencies() {
        currencies.clear();
    }

    public String getName() {
        return name;
    }

    public abstract boolean isMultiCurrency();
}
