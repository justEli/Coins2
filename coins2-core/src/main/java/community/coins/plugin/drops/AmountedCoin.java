package community.coins.plugin.drops;

import community.coins.plugin.item.DefinedCoin;

/**
 * @author Eli
 * @since May 04, 2026
 */
public record AmountedCoin(double minValue, double maxValue, int decimals, DefinedCoin coin) {}
