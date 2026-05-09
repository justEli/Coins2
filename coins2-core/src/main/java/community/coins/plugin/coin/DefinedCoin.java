package community.coins.plugin.coin;

import community.coins.plugin.economy.DefinedCurrency;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since April 28, 2026
 */
@NullMarked
public final class DefinedCoin {
    private final String id;
    private final ItemStack itemStack;
    private final Component singularName;
    private final Component pluralName;
    private final DefinedCurrency currency;

    public DefinedCoin(String id, ItemStack itemStack, Component singularName, Component pluralName, DefinedCurrency currency) {
        this.id = id;
        this.itemStack = itemStack;
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.currency = currency;
    }

    public ItemStack getItemStackClone() {
        return itemStack.clone();
    }

    public String getId() {
        return id;
    }

    public Component getSingularName() {
        return singularName;
    }

    public Component getPluralName() {
        return pluralName;
    }

    public DefinedCurrency getCurrency() {
        return currency;
    }
}
