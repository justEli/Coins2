package community.coins.plugin.drops;

import community.coins.plugin.item.DefinedCoin;

/**
 * @author Eli
 * @since May 04, 2026
 */
public final class AmountedCoin {
    private final double minValue;
    private final double maxValue;
    private final int decimals;
    private final DefinedCoin coin;

    public AmountedCoin(double minValue, double maxValue, int decimals, DefinedCoin coin) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.decimals = decimals;
        this.coin = coin;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public int getDecimals() {
        return decimals;
    }

    public DefinedCoin getCoin() {
        return coin;
    }
}
