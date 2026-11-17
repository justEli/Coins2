package community.coins.plugin.command;

import community.coins.plugin.CoinsCore;

import java.util.function.Function;

/**
 * @author Eli
 * @since November 17, 2026
 */
public class MintageCommandIdentifier extends CommandIdentifier<MintageCommandLogic> {
    public MintageCommandIdentifier(CoinsCore coins, Function<DefinedCommand, MintageCommandLogic> defined) {
        super(coins, defined, "mintage");
    }
}
