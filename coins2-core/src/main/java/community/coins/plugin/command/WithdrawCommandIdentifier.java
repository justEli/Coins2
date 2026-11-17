package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;

import java.util.function.Function;

/**
 * @author Eli
 * @since November 17, 2026
 */
public class WithdrawCommandIdentifier extends CommandIdentifier<WithdrawCommandLogic> {
    public WithdrawCommandIdentifier(CoinsCore coins, Function<DefinedCommand, WithdrawCommandLogic> defined) {
        super(coins, defined, "withdraw");
    }
}
