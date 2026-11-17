package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;

import java.util.function.Function;

/**
 * @author Eli
 * @since November 17, 2026
 */
public class BalanceCommandIdentifier extends CommandIdentifier<BalanceCommandLogic> {
    public BalanceCommandIdentifier(CoinsCore coins, Function<DefinedCommand, BalanceCommandLogic> defined) {
        super(coins, defined, "balance");
    }
}
