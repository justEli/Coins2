package community.coins.plugin.drops;

import community.coins.plugin.coin.DefinedCoin;

/**
 * @author Eli
 * @since May 04, 2026
 */
public record AmountedCoin(double minValue, double maxValue, int decimals, DefinedCoin coin) {}
