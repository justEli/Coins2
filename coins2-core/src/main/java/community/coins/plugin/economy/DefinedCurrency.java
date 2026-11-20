package community.coins.plugin.economy;

import community.coins.plugin.util.ComponentUtil;
import community.coins.plugin.util.MessagePosition;
import net.kyori.adventure.text.Component;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * a defined currency within a plugin/economy.
 * there can be multiple currencies in a plugin/economy if supported
 * @author Eli
 * @since May 05, 2026
 */
public final class DefinedCurrency {
    private final String identifier;
    private final EconomyHook economyHook;
    private final int decimals;
    private final DecimalFormat decimalFormat;
    private final String singularName;
    private final String pluralName;
    private final Component formatMessage;
    private final Component depositMessage;
    private final MessagePosition depositPosition;
    private final Action action = new Action();

    public DefinedCurrency(String id, EconomyHook economyHook, int decimals, String symbol, String singularName, String pluralName, String format, String depositMessage, MessagePosition depositPosition) {
        this.identifier = id; // already lowercase
        this.economyHook = economyHook;
        this.decimals = decimals;
        String decimalSuffix = decimals <= 0? "#" : "0".repeat(decimals);
        this.decimalFormat = new DecimalFormat("#,##0." + decimalSuffix);
        this.singularName = singularName;
        this.pluralName = pluralName;
        String formatWithSymbol = format.replace("{symbol}", symbol);
        this.formatMessage = ComponentUtil.parse(formatWithSymbol);
        this.depositMessage = ComponentUtil.parse(depositMessage.replace("{format}", formatWithSymbol));
        this.depositPosition = depositPosition;
    }

    public String getDecimalFormatted(double amount) {
        return decimalFormat.format(amount);
    }

    public String getIdentifier() {
        return identifier;
    }

    public EconomyHook getHook() {
        return economyHook;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getSingularName() {
        return singularName;
    }

    public String getPluralName() {
        return pluralName;
    }

    public Component getDepositMessage() {
        return depositMessage;
    }

    public Component getDepositMessage(double amount) {
        return ComponentUtil.replaceAmount(depositMessage, getDecimalFormatted(amount));
    }

    public MessagePosition getDepositPosition() {
        return depositPosition;
    }

    public Component getFormatMessage(double amount) {
        return ComponentUtil.replaceAmount(formatMessage, getDecimalFormatted(amount));
    }

    /// @return getFormatMessage, but parsed to String
    public String format(double amount) {
        return ComponentUtil.toStripped(getFormatMessage(amount));
    }

    /// may be executed async depending on the economy hook
    public void submitTransaction(Consumer<CurrencyAction> transaction) {
        transaction.accept(action);
    }

    private class Action implements CurrencyAction {
        @Override
        public double getBalance(UUID uuid) {
            return economyHook.getBalance(uuid, identifier);
        }

        @Override
        public boolean canAfford(UUID uuid, double amount) {
            return economyHook.canAfford(uuid, identifier, amount);
        }

        @Override
        public boolean deposit(UUID uuid, double amount) {
            return economyHook.deposit(uuid, identifier, amount);
        }

        @Override
        public boolean withdraw(UUID uuid, double amount) {
            return economyHook.withdraw(uuid, identifier, amount);
        }
    }
}
