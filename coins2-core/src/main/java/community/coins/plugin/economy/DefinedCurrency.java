package community.coins.plugin.economy;

import community.coins.plugin.component.ComponentUtil;
import community.coins.plugin.config.MessagePosition;
import net.kyori.adventure.text.Component;

import java.text.DecimalFormat;

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

    public DefinedCurrency(String identifier, EconomyHook economyHook, int decimals, String symbol, String singularName, String pluralName, String format, String depositMessage, MessagePosition depositPosition) {
        this.identifier = identifier.toLowerCase();
        this.economyHook = economyHook;
        this.decimals = decimals;
        this.decimalFormat = new DecimalFormat("#,##0." + "0".repeat(decimals));
        this.singularName = singularName;
        this.pluralName = pluralName;
        String formatWithSymbol = format.replace("{symbol}", symbol);
        this.formatMessage = ComponentUtil.parse(formatWithSymbol);
        this.depositMessage = ComponentUtil.parse(depositMessage.replace("{format}", formatWithSymbol));
        this.depositPosition = depositPosition;
    }

    public String getIdentifier() {
        return identifier;
    }

    public EconomyHook getEconomyHook() {
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
        return ComponentUtil.replaceAmount(depositMessage, formatAmount(amount));
    }

    public MessagePosition getDepositPosition() {
        return depositPosition;
    }

    public String formatAmount(double amount) {
        return decimalFormat.format(amount);
    }

    public Component getFormatMessage(double amount) {
        return ComponentUtil.replaceAmount(formatMessage, formatAmount(amount));
    }
}
